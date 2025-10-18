package com.example.websocket_demo.specification;

import com.example.websocket_demo.entity.BaseEntity;
import com.example.websocket_demo.entity.ChatRoomEntity;
import com.example.websocket_demo.entity.ProductEntity;
import com.example.websocket_demo.entity.UserEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class ProductSpecification extends BaseSpecification<ProductEntity> {
    public static Specification<ProductEntity> isNotDeleted() {
        return (Root<ProductEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> criteriaBuilder.isNull(root.get(BaseEntity.Fields.deletedAt));
    }
    public static Specification<ProductEntity> hasUser(Long userId) {
        return (Root<ProductEntity> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) -> criteriaBuilder.equal(root.get(ProductEntity.Fields.user).get(UserEntity.Fields.userId), userId);
    }
}
