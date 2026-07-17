package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.UserScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserScoreRepository extends JpaRepository<UserScoreEntity, Long> {
    Optional<UserScoreEntity> findByUsername(String username);
}
