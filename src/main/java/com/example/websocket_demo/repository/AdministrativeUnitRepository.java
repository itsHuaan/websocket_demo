package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.AdministrativeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AdministrativeUnitRepository extends JpaRepository<AdministrativeUnit, Integer> {
}
