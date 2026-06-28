package com.example.websocket_demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.websocket_demo.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long>, JpaSpecificationExecutor<UserEntity> {
    Optional<UserEntity> findByUsernameAndDeletedAtIsNull(String username);
    Optional<UserEntity> findByUserIdAndDeletedAtIsNull(Long userId);
    Optional<UserEntity> findByEmailAndDeletedAtIsNull(String email);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByPhoneNumberAndDeletedAtIsNull(String phoneNumber);
    boolean existsByEmailAndDeletedAtIsNull(String email);
    boolean existsByUsernameAndDeletedAtIsNull(String username);
    boolean existsByPhoneNumberAndDeletedAtIsNull(String phoneNumber);

    @Query("SELECT DISTINCT u FROM UserEntity u " +
           "WHERE u.deletedAt IS NULL AND u.userId != :userId " +
           "AND EXISTS (" +
               "SELECT 1 FROM ChatMessageEntity m " +
               "WHERE (m.senderId = :userId AND m.recipientId = u.userId) " +
               "OR (m.senderId = u.userId AND m.recipientId = :userId)" +
           ")")
    List<UserEntity> findConnectedUsers(@Param("userId") Long userId);
}
