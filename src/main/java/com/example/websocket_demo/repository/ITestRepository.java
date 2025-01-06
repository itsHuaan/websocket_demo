package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.TestEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ITestRepository extends JpaRepository<TestEntity, Long> {
}
