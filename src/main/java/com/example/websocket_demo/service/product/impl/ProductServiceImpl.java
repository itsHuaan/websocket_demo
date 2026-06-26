package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.service.media.impl.CloudinaryServiceImpl;
import com.example.websocket_demo.dto.request.ProductRequest;
import com.example.websocket_demo.dto.response.ProductResponse;
import com.example.websocket_demo.dto.response.ProductSummaryResponse;
import com.example.websocket_demo.entity.*;
import com.example.websocket_demo.mapper.ProductMapper;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.product.ProductService;
import com.example.websocket_demo.repository.specification.ProductSpecification;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements ProductService {
    UserRepository userRepository;
    ProductRepository productRepository;
    ProductOptionRepository productOptionRepository;
    ProductSkuRepository productSkuRepository;
    ProductOptionValueRepository productOptionValueRepository;
    ProductSkuValueRepository productSkuValueRepository;
    ProductMapper productMapper;
    CloudinaryServiceImpl mediaUploader;

    @Transactional
    @Override
    public void addProduct(ProductRequest productRequest) {
        if (DataUtil.hasNullField(productRequest)) {
            throw new IllegalArgumentException("Please fill all the required fields");
        }
        if (productRequest.getMedia() == null) {
            throw new IllegalArgumentException("Product needs to have at least media items");
        }

        UserEntity user = userRepository.findById(productRequest.getUserId()).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        ProductEntity product = ProductEntity.builder()
                .productName(productRequest.getProductName())
                .user(user)
                .build();
        
        List<ProductMediaEntity> productMedias = Arrays.stream(productRequest.getMedia())
                .map(media -> {
                    try {
                        return ProductMediaEntity.builder()
                                .product(product)
                                .mediaUrl(mediaUploader.uploadMediaFile(media))
                                .build();
                    } catch (IOException e) {
                        log.error("Failed to parse media: {}", e.getMessage());
                        throw new RuntimeException("Failed to upload media", e);
                    }
                }).toList();
        product.setMediaUrls(productMedias);
        productRepository.save(product);
        
        if (productRequest.getOptions() == null || productRequest.getOptions().isEmpty()) {
            handleNoOptionCase(product, productRequest.getSkus());
        } else {
            handleOptionCases(product, productRequest.getOptions(), productRequest.getSkus());
        }
    }

    @Override
    public List<ProductSummaryResponse> getAll() {
        return productRepository.findAll(Specification.where(
                        ProductSpecification.isNull(BaseEntity.Fields.deletedAt)
                )).stream()
                .map(productMapper::toProductSummaryDto)
                .toList();
    }

    @Override
    public List<ProductSummaryResponse> getAllByUser(Long userId) {
        return productRepository.findAll(Specification.where(
                        ProductSpecification.isNotDeleted()
                                .and(ProductSpecification.equal("user", userId))
                )).stream()
                .map(productMapper::toProductSummaryDto)
                .toList();
    }

    @Override
    public ProductResponse getById(Long id) {
        return productMapper.toProductDto(productRepository.findByProductIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("Product not found")
        ));
    }

    private void handleNoOptionCase(ProductEntity product, List<ProductRequest.SkuRequest> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new IllegalArgumentException("Product must have at least one SKU with a price.");
        }

        for (ProductRequest.SkuRequest skuRequest : skus) {
            ProductSkuEntity sku = ProductSkuEntity.builder()
                    .product(product)
                    .skuValues(new HashSet<>())
                    .price(skuRequest.getPrice())
                    .build();
            productSkuRepository.save(sku);
            ProductSkuValueEntity skuValue = ProductSkuValueEntity.builder()
                    .sku(sku)
                    .option(null)
                    .optionValue(null)
                    .build();
            productSkuValueRepository.save(skuValue);
        }
    }

    private void handleOptionCases(ProductEntity product, List<ProductRequest.OptionRequest<?>> options, List<ProductRequest.SkuRequest> skus) {
        for (ProductRequest.OptionRequest<?> optionRequest : options) {
            ProductOptionEntity option = ProductOptionEntity.builder()
                    .optionName(optionRequest.getName())
                    .product(product)
                    .build();
            option = productOptionRepository.save(option);

            for (Object value : optionRequest.getValues()) {
                ProductOptionValueEntity optionValue = ProductOptionValueEntity.builder()
                        .valueName(value.toString())
                        .option(option)
                        .build();
                productOptionValueRepository.save(optionValue);
            }
        }

        for (ProductRequest.SkuRequest skuRequest : skus) {
            ProductSkuEntity sku = ProductSkuEntity.builder()
                    .product(product)
                    .price(skuRequest.getPrice())
                    .build();
            sku = productSkuRepository.save(sku);

            for (ProductRequest.SkuRequest.SkuValueRequest skuValueRequest : skuRequest.getValues()) {
                ProductOptionEntity option = productOptionRepository.findByProductAndOptionName(product, skuValueRequest.getOption())
                        .orElseThrow(() -> new NoSuchElementException("Option not found: " + skuValueRequest.getOption()));

                ProductOptionValueEntity optionValue = productOptionValueRepository.findByOptionAndValueName(option, skuValueRequest.getValue())
                        .orElseThrow(() -> new NoSuchElementException("Option Value not found: " + skuValueRequest.getValue()));

                ProductSkuValueEntity skuValue = ProductSkuValueEntity.builder()
                        .sku(sku)
                        .option(option)
                        .optionValue(optionValue)
                        .build();
                productSkuValueRepository.save(skuValue);
            }
        }
    }
}
