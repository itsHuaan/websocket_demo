package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_product_option", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "option_name"})
})
public class ProductOptionEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long optionId;

    @Column(nullable = false)
    String optionName;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    ProductEntity product;

    @OneToMany(mappedBy = "option", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    Set<ProductOptionValueEntity> optionValues;
}
