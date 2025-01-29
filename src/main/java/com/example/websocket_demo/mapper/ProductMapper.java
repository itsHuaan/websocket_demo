package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.*;
import com.example.websocket_demo.entity.*;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

import java.util.List;
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
                        .flatMap(sku -> sku.getSkuValues().stream())
                        .mapToDouble(ProductSkuValueEntity::getPrice)
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
        ProductSkuDto dto = new ProductSkuDto();
        dto.setId(sku.getSkuId());
        if (sku.getSkuValues() != null && !sku.getSkuValues().isEmpty()) {
            dto.setValues(sku.getSkuValues().stream()
                    .map(skuValue -> skuValue.getOptionValue() != null
                            ? skuValue.getOptionValue().getValueName()
                            : null)
                    .collect(Collectors.toList()));

            dto.setPrice(sku.getSkuValues().iterator().next().getPrice());
        }
        return dto;
    }

//    private ProductSkuValueDto toProductSkuValueDto(ProductSkuValueEntity skuValue) {
//        return ProductSkuValueDto.builder()
//                .id(skuValue.getSkuValueId())
//                .price(skuValue.getPrice())
//                .option(skuValue.getOption() != null
//                        ? skuValue.getOption().getOptionName()
//                        : null)
//                .value(skuValue.getOptionValue() != null
//                        ? skuValue.getOptionValue().getValueName()
//                        : null)
//                .build();
//    }

    private ProductOptionDto toProductOptionDto(ProductOptionEntity option) {
        return ProductOptionDto.builder()
                .optionName(option.getOptionName())
                .values(option.getOptionValues().stream()
                        .map(ProductOptionValueEntity::getValueName)
                        .toList())
                .build();
    }
}
