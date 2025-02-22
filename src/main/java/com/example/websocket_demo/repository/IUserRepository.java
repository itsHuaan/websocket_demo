package com.example.websocket_demo.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.websocket_demo.entity.UserEntity;

public interface IUserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUsernameAndDeletedAtIsNull(String username);
    Optional<UserEntity> findByUserIdAndDeletedAtIsNull(Long userId);
}
