package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.request.WolRequest;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.entity.WolDeviceEntity;
import com.example.websocket_demo.repository.WolDeviceRepository;
import com.example.websocket_demo.service.network.WolDeviceService;
import com.example.websocket_demo.service.network.WolService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    @Override
    public WolDeviceResponse createDevice(WolDeviceRequest request) {
        WolDeviceEntity entity = WolDeviceEntity.builder()
                .name(request.getName())
                .macAddress(request.getMacAddress())
                .host(request.getHost())
                .port(request.getPort())
                .build();
        
        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public WolDeviceResponse updateDevice(Long id, WolDeviceRequest request) {
        WolDeviceEntity entity = getEntityById(id);
        
        entity.setName(request.getName());
        entity.setMacAddress(request.getMacAddress());
        entity.setHost(request.getHost());
        entity.setPort(request.getPort());
        
        entity = repository.save(entity);
        return mapToResponse(entity);
    }

    @Override
    public void deleteDevice(Long id) {
        WolDeviceEntity entity = getEntityById(id);
        entity.setDeletedAt(LocalDateTime.now());
        repository.save(entity);
    }

    @Override
    public WolDeviceResponse getDevice(Long id) {
        return mapToResponse(getEntityById(id));
    }

    @Override
    public List<WolDeviceResponse> getAllDevices() {
        return repository.findAllByDeletedAtIsNull().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void wakeDevice(Long id) {
        WolDeviceEntity entity = getEntityById(id);
        
        wolService.wakeOnLan(entity.getMacAddress(), entity.getHost(), entity.getPort());
    }
    
    private WolDeviceEntity getEntityById(Long id) {
        WolDeviceEntity entity = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Device not found with id: " + id));
                
        if (entity.getDeletedAt() != null) {
            throw new RuntimeException("Device has been deleted");
        }
        
        return entity;
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
