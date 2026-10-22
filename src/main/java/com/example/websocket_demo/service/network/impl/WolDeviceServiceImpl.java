package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.entity.WolDeviceEntity;
import com.example.websocket_demo.repository.UserRepository;
import com.example.websocket_demo.repository.WolDeviceRepository;
import com.example.websocket_demo.service.network.WolDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolDeviceServiceImpl implements WolDeviceService {

    private final WolDeviceRepository repository;
    private final UserRepository userRepository;

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
        wakeOnLan(entity.getMacAddress(), entity.getHost(), entity.getPort());
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

    private void wakeOnLan(String macAddress, String host, int port) {
        try {
            byte[] macBytes = getMacBytes(macAddress);
            byte[] bytes = new byte[6 + 16 * macBytes.length];

            // 6 bytes of 0xFF
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) 0xff;
            }

            // 16 repetitions of MAC address
            for (int i = 6; i < bytes.length; i += macBytes.length) {
                System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
            }

            InetAddress address = InetAddress.getByName(host);
            DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setBroadcast(true);
                socket.send(packet);
                log.info("Wake-on-LAN packet sent to MAC: {}, Host: {}, Port: {}",
                        macAddress, host, port);
            }

        } catch (Exception e) {
            log.error("Failed to send Wake-on-LAN packet to MAC: {}", macAddress, e);
            throw new RuntimeException("Failed to send Wake-on-LAN packet: " + e.getMessage(), e);
        }
    }

    private byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(:|-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
}
