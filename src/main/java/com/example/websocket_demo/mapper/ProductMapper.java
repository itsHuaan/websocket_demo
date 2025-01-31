package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.*;
import com.example.websocket_demo.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Component
public class ProductMapper {
    public ProductSummaryDto toProductSummaryDto(ProductEntity product) {
        return ProductSummaryDto.builder()
                .user(product.getUser().getUsername())
                .productName(product.getProductName())
                .options(product.getOptions().stream()
                        .map(this::toProductOptionDto)
                        .toList())
                .price(product.getSkus().stream()
                        .mapToDouble(ProductSkuEntity::getPrice)
                        .min()
                        .orElse(0.0))
                .build();
    }

    public ProductDto toProductDto(ProductEntity product) {
        List<ProductOptionDto> productOptions = product.getOptions().stream()
                .map(this::toProductOptionDto)
                .toList();
        return ProductDto.builder()
                .productName(product.getProductName())
                .user(product.getUser().getUsername())
                .options(!productOptions.isEmpty() ? productOptions : null)
                .skus(product.getSkus().stream()
                        .map(this::toProductSkuDto)
                        .toList())
                .build();
    }

    private ProductSkuDto toProductSkuDto(ProductSkuEntity sku) {
        return ProductSkuDto.builder()
                .id(sku.getSkuId())
                .values(sku.getSkuValues().stream()
                        .map(skuValue -> skuValue.getOptionValue() != null
                                ? skuValue.getOptionValue().getValueName()
                                : null)
                        .filter(Objects::nonNull)
                        .toList())
                .price(sku.getPrice())
                .build();
    }

    private ProductOptionDto toProductOptionDto(ProductOptionEntity option) {
        return ProductOptionDto.builder()
                .optionName(option.getOptionName())
                .values(option.getOptionValues().stream()
                        .map(ProductOptionValueEntity::getValueName)
                        .toList())
                .build();
    }
}
