package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.ProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {
    Optional<ProvinceEntity> findByCodeName(String codeName);

    @Query(value = "SELECT p FROM ProvinceEntity p WHERE p.administrativeUnit.codeName = :codeName")
    List<ProvinceEntity> findByUnitCode(String codeName);

    @Query(value = "SELECT p FROM ProvinceEntity p WHERE p.administrativeRegion.codeName = :codeName")
    List<ProvinceEntity> findByRegionCode(String codeName);
}
