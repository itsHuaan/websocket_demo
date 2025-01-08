package com.example.websocket_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.websocket_demo.entity.TokenBlackListEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ITokenBlackListRepository extends JpaRepository<TokenBlackListEntity, Long>, JpaSpecificationExecutor<TokenBlackListEntity> {
    boolean existsByToken(String jwtToken);
}
