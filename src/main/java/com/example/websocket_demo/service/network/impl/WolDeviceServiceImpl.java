package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.client.BaseClient;
import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.entity.WolDeviceEntity;
import com.example.websocket_demo.repository.UserRepository;
import com.example.websocket_demo.repository.WolDeviceRepository;
import com.example.websocket_demo.service.network.WolDeviceService;
import com.example.websocket_demo.service.network.WolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolDeviceServiceImpl implements WolDeviceService {

    private final WolDeviceRepository repository;
    private final WolService wolService;
    private final UserRepository userRepository;
    private final BaseClient baseClient;

    @Override
    public WolDeviceResponse createDevice(Long userId, WolDeviceRequest request) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        WolDeviceEntity entity = WolDeviceEntity.builder()
                .name(request.getName())
                .macAddress(request.getMacAddress())
                .host(request.getHost())
                .port(request.getPort())
                .user(user)
                .build();

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public WolDeviceResponse updateDevice(Long userId, Long id, WolDeviceRequest request) {
        WolDeviceEntity entity = getEntityByIdAndUserId(id, userId);

        entity.setName(request.getName());
        entity.setMacAddress(request.getMacAddress());
        entity.setHost(request.getHost());
        entity.setPort(request.getPort());

        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public void deleteDevice(Long userId, Long id) {
        WolDeviceEntity entity = getEntityByIdAndUserId(id, userId);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    public WolDeviceResponse getDevice(Long userId, Long id) {
        return mapToResponse(getEntityByIdAndUserId(id, userId));
    }

    @Override
    public List<WolDeviceResponse> getAllDevices(Long userId) {
        return repository.findAllByUser_UserIdAndDeletedAtIsNull(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void wakeDevice(Long userId, Long id) {
        WolDeviceEntity entity = getEntityByIdAndUserId(id, userId);

        wolService.wakeOnLan(entity.getMacAddress(), entity.getHost(), entity.getPort());
    }

    @Async
    @Override
    public void applyPortForwarding() {
        baseClient.applyPortForwarding();
    }

    @Override
    public List<ArpEntryResponse> getArpTable() {
        return baseClient.getArpTable();
    }

    private WolDeviceEntity getEntityByIdAndUserId(Long id, Long userId) {
        return repository.findByIdAndUser_UserIdAndDeletedAtIsNull(id, userId)
                .orElseThrow(() -> new RuntimeException("Device not found or access denied for id: " + id));
    }

    private WolDeviceResponse mapToResponse(WolDeviceEntity entity) {
        return WolDeviceResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .macAddress(entity.getMacAddress())
                .host(entity.getHost())
                .port(entity.getPort())
                .createdAt(entity.getCreatedAt())
                .modifiedAt(entity.getModifiedAt())
                .build();
    }
}
