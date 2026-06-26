package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProductSkuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductSkuRepository extends JpaRepository<ProductSkuEntity, Long>, JpaSpecificationExecutor<ProductSkuEntity> {
}
