package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.AdministrativeRegionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministrativeRegionRepository extends JpaRepository<AdministrativeRegionEntity, Integer> {
    Optional<AdministrativeRegionEntity> findByCodeName(String codeName);
}
