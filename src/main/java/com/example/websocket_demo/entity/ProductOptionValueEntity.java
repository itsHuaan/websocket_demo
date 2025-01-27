package com.example.websocket_demo.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = "tbl_product_option_value", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "option_id", "value_name"})
})
public class ProductOptionValueEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long valueId;

    @Column(nullable = false)
    String valueName;

    @ManyToOne
    @JoinColumn(name = "option_id", nullable = false)
    ProductOptionEntity option;
}
