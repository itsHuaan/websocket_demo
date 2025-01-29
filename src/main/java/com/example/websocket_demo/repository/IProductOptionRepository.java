package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProductEntity;
import com.example.websocket_demo.entity.ProductOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IProductOptionRepository extends JpaRepository<ProductOptionEntity, Long>, JpaSpecificationExecutor<ProductOptionEntity> {
    Optional<ProductOptionEntity> findByProductAndOptionName(ProductEntity product, String optionName);
    Optional<ProductOptionEntity> findByOptionName(String optionName);
}
