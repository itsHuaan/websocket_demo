package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.GisWard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GisWardRepository extends JpaRepository<GisWard, Integer> {
    GisWard findByWardCode(String wardCode);
}
