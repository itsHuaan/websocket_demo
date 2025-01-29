package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.entity.*;
import com.example.websocket_demo.mapper.ProductMapper;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.repository.*;
import com.example.websocket_demo.service.product.IProductActionService;
import com.example.websocket_demo.util.NullChecker;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;

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
            log.error("Failed to add product {}", e.getMessage());
            return 0;
        }
    }

    @Override
    public List<ProductSummaryDto> getAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toProductSummaryDto)
                .toList();
    }

    private void handleNoOptionCase(ProductEntity product, List<ProductRequest.SkuRequest> skus) {
        if (skus == null || skus.isEmpty()) {
            throw new RuntimeException("Product must have at least one SKU with a price.");
        }

        for (ProductRequest.SkuRequest skuRequest : skus) {
            ProductSkuEntity sku = ProductSkuEntity.builder()
                    .product(product)
                    .skuValues(new HashSet<>())
                    .build();
            productSkuRepository.save(sku);
            ProductSkuValueEntity skuValue = ProductSkuValueEntity.builder()
                    .sku(sku)
                    .option(null)
                    .optionValue(null)
                    .price(skuRequest.getPrice())
                    .build();
            if (skuValue.getOption() == null) {
                productSkuValueRepository.save(skuValue);
            }
        }
    }


    private void handleOptionCases(ProductEntity product, List<ProductRequest.OptionRequest<?>> options, List<ProductRequest.SkuRequest> skus) {
        for (ProductRequest.OptionRequest<?> optionRequest : options) {
            ProductOptionEntity option = ProductOptionEntity.builder()
                    .optionName(optionRequest.getOptionName())
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
                    .build();
            sku = productSkuRepository.save(sku);

            for (ProductRequest.SkuRequest.SkuValueRequest skuValueRequest : skuRequest.getSkuValues()) {
                ProductOptionEntity option = productOptionRepository.findByProductAndOptionName(product, skuValueRequest.getOptionName())
                        .orElseThrow(() -> new RuntimeException("Option not found"));

                ProductOptionValueEntity optionValue = productOptionValueRepository.findByOptionAndValueName(option, skuValueRequest.getValueName())
                        .orElseThrow(() -> new RuntimeException("Option Value not found"));

                ProductSkuValueEntity skuValue = ProductSkuValueEntity.builder()
                        .sku(sku)
                        .option(option)
                        .optionValue(optionValue)
                        .price(skuRequest.getPrice())
                        .build();
                productSkuValueRepository.save(skuValue);
            }
        }
    }
}
