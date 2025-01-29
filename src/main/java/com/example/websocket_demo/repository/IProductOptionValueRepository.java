package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProductOptionEntity;
import com.example.websocket_demo.entity.ProductOptionValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface IProductOptionValueRepository extends JpaRepository<ProductOptionValueEntity, Long>, JpaSpecificationExecutor<ProductOptionValueEntity> {
    Optional<ProductOptionValueEntity> findByOptionAndValueName(ProductOptionEntity option, String valueName);
}
