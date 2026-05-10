package com.example.websocket_demo.mapper;

import com.example.websocket_demo.dto.response.*;
import com.example.websocket_demo.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import org.mapstruct.ReportingPolicy;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    @Mapping(target = "user", source = "user.username")
    @Mapping(target = "price", expression = "java(calculateMinPrice(product))")
    ProductSummaryResponse toProductSummaryDto(ProductEntity product);

    @Mapping(target = "user", source = "user.username")
    ProductResponse toProductDto(ProductEntity product);

    @Mapping(target = "id", source = "skuId")
    @Mapping(target = "values", expression = "java(mapSkuValues(sku.getSkuValues()))")
    ProductSkuResponse toProductSkuDto(ProductSkuEntity sku);

    @Mapping(target = "values", expression = "java(mapOptionValues(option.getOptionValues()))")
    ProductOptionResponse toProductOptionDto(ProductOptionEntity option);

    default Double calculateMinPrice(ProductEntity product) {
        if (product.getSkus() == null || product.getSkus().isEmpty()) {
            return 0.0;
        }
        return product.getSkus().stream()
                .mapToDouble(ProductSkuEntity::getPrice)
                .min()
                .orElse(0.0);
    }

    default List<String> mapSkuValues(Collection<ProductSkuValueEntity> skuValues) {
        if (skuValues == null) return null;
        return skuValues.stream()
                .map(skuValue -> skuValue.getOptionValue() != null
                        ? skuValue.getOptionValue().getValueName()
                        : null)
                .filter(Objects::nonNull)
                .toList();
    }

    default List<String> mapOptionValues(Collection<ProductOptionValueEntity> optionValues) {
        if (optionValues == null) return null;
        return optionValues.stream()
                .map(ProductOptionValueEntity::getValueName)
                .toList();
    }
}
