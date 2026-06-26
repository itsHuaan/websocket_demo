package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.WardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WardRepository extends JpaRepository<WardEntity, String> {
    List<WardEntity> findByProvinceCode(String provinceCode);

    Optional<WardEntity> findByCodeName(String codeName);

    @Query(value = "SELECT w FROM WardEntity w WHERE w.province.codeName = :provinceCodeName")
    List<WardEntity> findByProvinceCodeName(String provinceCodeName);
}
