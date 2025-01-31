package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.dto.ProductDto;
import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.entity.*;
import com.example.websocket_demo.mapper.ProductMapper;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.product.IProductActionService;
import com.example.websocket_demo.specification.ProductSpecification;
import com.example.websocket_demo.util.NullChecker;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductActionService implements IProductActionService {
    IUserRepository userRepository;
    IProductRepository productRepository;
    IProductOptionRepository productOptionRepository;
    IProductSkuRepository productSkuRepository;
    IProductOptionValueRepository productOptionValueRepository;
    IProductSkuValueRepository productSkuValueRepository;
    ProductMapper productMapper;

    @Transactional
    @Override
    public int addProduct(ProductRequest productRequest) {
        if (NullChecker.hasNullField(productRequest)) {
            throw new IllegalArgumentException("Please fill all the required fields");
        }
        try {
            UserEntity user = userRepository.findById(productRequest.getUserId()).orElseThrow(
                    () -> new NoSuchElementException("User not found")
            );
            ProductEntity product = ProductEntity.builder()
                    .productName(productRequest.getProductName())
                    .user(user)
                    .build();
            productRepository.save(product);
            if (productRequest.getOptions() == null || productRequest.getOptions().isEmpty()) {
                handleNoOptionCase(product, productRequest.getSkus());
            } else {
                handleOptionCases(product, productRequest.getOptions(), productRequest.getSkus());
            }

            return 1;
        } catch (Exception e) {
            log.error("Failed to add product: {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public List<ProductSummaryDto> getAll() {
        return productRepository.findAll(Specification.where(
                        ProductSpecification.isNotDeleted()
                )).stream()
                .map(productMapper::toProductSummaryDto)
                .toList();
    }

    @Override
    public List<ProductSummaryDto> getAllByUser(Long userId) {
        return productRepository.findAll(Specification.where(
                        ProductSpecification.isNotDeleted()
                                .and(ProductSpecification.isNotDeleted())
                )).stream()
                .map(productMapper::toProductSummaryDto)
                .toList();
    }

    @Override
    public ProductDto getById(Long id) {
        return productMapper.toProductDto(productRepository.findByProductIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("Product not found")
        ));
    }

    private void handleNoOptionCase(ProductEntity product, List<ProductRequest.SkuRequest> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new RuntimeException("Product must have at least one SKU with a price.");
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
            if (skuValue.getOption() == null) {
                productSkuValueRepository.save(skuValue);
            }
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
                        .orElseThrow(() -> new RuntimeException("Option not found"));

                ProductOptionValueEntity optionValue = productOptionValueRepository.findByOptionAndValueName(option, skuValueRequest.getValue())
                        .orElseThrow(() -> new RuntimeException("Option Value not found"));

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
