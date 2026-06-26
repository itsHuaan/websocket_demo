package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.GisWardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GisWardRepository extends JpaRepository<GisWardEntity, Integer> {
    GisWardEntity findByWardCode(String wardCode);
}
