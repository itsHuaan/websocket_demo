package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.FieldNameConstants;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_product_sku_value")
@FieldNameConstants
public class ProductSkuValueEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long skuValueId;

    @ManyToOne
    @JoinColumn(name = "sku_id", nullable = false)
    ProductSkuEntity sku;

    @ManyToOne
    @JoinColumn(name = "option_id")
    ProductOptionEntity option;

    @ManyToOne
    @JoinColumn(name = "option_value_id")
    ProductOptionValueEntity optionValue;
}
