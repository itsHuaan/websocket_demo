package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.entity.WolDeviceEntity;
import com.example.websocket_demo.repository.UserRepository;
import com.example.websocket_demo.repository.WolDeviceRepository;
import com.example.websocket_demo.service.network.WolDeviceService;
import com.example.websocket_demo.service.network.WolService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WolDeviceServiceImpl implements WolDeviceService {

    @Value("${router.host}")
    private String host;

    @Value("${router.username}")
    private String username;

    @Value("${router.password}")
    private String password;

    @Value("${router.path}")
    private String path;

    private final WolDeviceRepository repository;
    private final WolService wolService;
    private final UserRepository userRepository;

    @Override
    @Async
    public void refreshArp() {
        String url = host + path;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            int randomId = new Random().nextInt(100000);

            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("username", username);
            loginParams.put("password", password);

            Map<String, Object> step1Payload = new HashMap<>();
            step1Payload.put("jsonrpc", "2.0");
            step1Payload.put("id", randomId);
            step1Payload.put("method", "call");
            step1Payload.put("params", new Object[]{
                    "00000000000000000000000000000000",
                    "session",
                    "login",
                    loginParams
            });

            HttpEntity<Map<String, Object>> step1Request = new HttpEntity<>(step1Payload, headers);
            ResponseEntity<String> step1Response = restTemplate.postForEntity(url, step1Request, String.class);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode step1Node = mapper.readTree(step1Response.getBody());
            String token = step1Node.path("result").path(1).path("ubus_rpc_session").asText();

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Failed to get session token");
            }

            Map<String, Object> redirectParams = new HashMap<>();
            redirectParams.put("redirect", new Object[]{});

            Map<String, Object> step2Payload = new HashMap<>();
            step2Payload.put("jsonrpc", "2.0");
            step2Payload.put("id", randomId + 1);
            step2Payload.put("method", "call");
            step2Payload.put("params", new Object[]{
                    token,
                    "data_repo.weboui",
                    "portfwd_set",
                    redirectParams
            });

            HttpEntity<Map<String, Object>> step2Request = new HttpEntity<>(step2Payload, headers);
            restTemplate.postForEntity(url, step2Request, String.class);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

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
