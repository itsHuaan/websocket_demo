package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.ProductOptionDto;
import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.entity.ProductEntity;
import com.example.websocket_demo.entity.ProductOptionEntity;
import com.example.websocket_demo.entity.ProductOptionValueEntity;
import com.example.websocket_demo.entity.ProductSkuValueEntity;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
@Component
public class ProductMapper {
    public ProductSummaryDto toProductSummaryDto(ProductEntity product) {
        return ProductSummaryDto.builder()
                .user(product.getUser().getUsername())
                .productName(product.getProductName())
                .options(product.getOptions().stream().map(this::toProductOptionDto).collect(Collectors.toList()))
                .price(product.getSkus().stream()
                        .flatMap(sku -> sku.getSkuValues().stream())
                        .mapToDouble(ProductSkuValueEntity::getPrice)
                        .min()
                        .orElse(0.0))
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
