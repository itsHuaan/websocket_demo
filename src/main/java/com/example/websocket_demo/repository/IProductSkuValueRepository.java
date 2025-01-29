package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProductSkuValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface IProductSkuValueRepository extends JpaRepository<ProductSkuValueEntity, Long>, JpaSpecificationExecutor<ProductSkuValueEntity> {
}
