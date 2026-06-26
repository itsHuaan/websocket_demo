package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.GisProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GisProvinceRepository extends JpaRepository<GisProvinceEntity, Integer> {
    GisProvinceEntity findByProvinceCode(String provinceCode);
}
