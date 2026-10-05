package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.service.network.WolDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

@RestController
@RequestMapping("/api/v1/network/devices")
@RequiredArgsConstructor
@Tag(name = "Network Devices API", description = "CRUD operations for Wake on LAN devices")
public class WolDeviceController {

    private final WolDeviceService deviceService;

    @Value("${router.host}")
    private String host;

    @Value("${router.username}")
    private String username;

    @Value("${router.password}")
    private String password;

    @Value("${router.path}")
    private String path;

    private Long getUserId(Principal principal) {
        if (principal == null) {
            throw new RuntimeException("Unauthorized access");
        }
        return Long.parseLong(principal.getName());
    }

    @PostMapping
    @Operation(summary = "Create a new device")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> createDevice(
            @Valid @RequestBody WolDeviceRequest request,
            Principal principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.CREATED,
                "Device created successfully",
                deviceService.createDevice(userId, request)
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing device")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> updateDevice(
            @PathVariable Long id, 
            @Valid @RequestBody WolDeviceRequest request,
            Principal principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Device updated successfully",
                deviceService.updateDevice(userId, id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(
            @PathVariable Long id,
            Principal principal) {
        Long userId = getUserId(principal);
        deviceService.deleteDevice(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Device deleted successfully"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a device by ID")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> getDevice(
            @PathVariable Long id,
            Principal principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Success",
                deviceService.getDevice(userId, id)
        ));
    }

    @GetMapping
    @Operation(summary = "Get all devices")
    public ResponseEntity<ApiResponse<List<WolDeviceResponse>>> getAllDevices(Principal principal) {
        Long userId = getUserId(principal);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Success",
                deviceService.getAllDevices(userId)
        ));
    }

    @PostMapping("/{id}/wake")
    @Operation(summary = "Wake up a specific device by ID")
    public ResponseEntity<ApiResponse<String>> wakeDevice(
            @PathVariable Long id,
            Principal principal) {
        Long userId = getUserId(principal);
        deviceService.wakeDevice(userId, id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Wake on LAN packet sent successfully",
                "Device ID: " + id
        ));
    }

    @PostMapping("/apply-port-forwarding")
    @Operation(summary = "Apply port forwarding (Refresh ARP cache)")
    public ResponseEntity<ApiResponse<Void>> applyPortForwarding() {
        String url = host + path;
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);
            
            // Step 1: Login to get session token
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
            
            // Step 2: Apply port forwarding
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
            ResponseEntity<String> step2Response = restTemplate.postForEntity(url, step2Request, String.class);
            
            JsonNode step2Node = mapper.readTree(step2Response.getBody());
            boolean status = step2Node.path("result").path(1).path("status").asBoolean();
            
            if (!status) {
                throw new RuntimeException("Failed to apply port forwarding");
            }
            
            return ResponseEntity.ok(new ApiResponse<>(
                    HttpStatus.OK,
                    "Port forwarding applied successfully"
            ));
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            "Error: " + e.getMessage()
                    ));
        }
    }
}
