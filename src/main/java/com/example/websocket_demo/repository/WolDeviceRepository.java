package com.example.websocket_demo.repository;

import com.example.websocket_demo.entity.WolDeviceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WolDeviceRepository extends JpaRepository<WolDeviceEntity, Long> {
    List<WolDeviceEntity> findAllByUser_UserIdAndDeletedAtIsNull(Long userId);
    Optional<WolDeviceEntity> findByIdAndUser_UserIdAndDeletedAtIsNull(Long id, Long userId);
}
