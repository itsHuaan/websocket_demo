package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface IProductRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {
    Optional<ProductEntity> findByProductIdAndDeletedAtIsNull(Long productId);
    List<ProductEntity> findByUser_UserId(Long userUserId);
}
