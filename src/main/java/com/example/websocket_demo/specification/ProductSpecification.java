package com.example.websocket_demo.specification;

import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.entity.ProductEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification {
    public static Specification<ProductEntity> isNotDeleted() {
        return (Root<ProductEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }
    public static Specification<ProductEntity> hasUser(Long userId) {
        return (Root<ProductEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("userId"), userId);
    }
}
