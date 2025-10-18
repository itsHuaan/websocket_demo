package com.example.websocket_demo.specification;

import com.example.websocket_demo.entity.BaseEntity;
import com.example.websocket_demo.entity.UserEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class UserSpecification extends BaseSpecification<UserEntity> {
}
