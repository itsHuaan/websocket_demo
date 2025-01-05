package com.example.websocket_demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.websocket_demo.entity.TokenBlackListEntity;

public interface ITokenBlackListRepository extends JpaRepository<TokenBlackListEntity, Long> {
    boolean existsByToken(String jwtToken);
}
