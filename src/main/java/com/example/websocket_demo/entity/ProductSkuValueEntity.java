package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_product_sku_value")
public class ProductSkuValueEntity extends BaseEntity {
    @EmbeddedId
    SkuValueId id;

    @ManyToOne
    @MapsId("skuId")
    @JoinColumn(name = "sku_id", nullable = false)
    ProductSkuEntity sku;

    @ManyToOne
    @MapsId("optionId")
    @JoinColumn(name = "option_id", nullable = false)
    ProductOptionEntity option;

    @ManyToOne
    @MapsId("optionValueId")
    @JoinColumn(name = "option_value_id", nullable = false)
    ProductOptionValueEntity optionValue;

    @Column(nullable = false)
    Double price;
}
