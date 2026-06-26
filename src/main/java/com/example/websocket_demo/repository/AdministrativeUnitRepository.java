package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.AdministrativeUnitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnitEntity, Integer> {
    Optional<AdministrativeUnitEntity> findByCodeName(String codeName);
}
