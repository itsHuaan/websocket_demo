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

    @PostMapping("/refresh-arp")
    @Operation(summary = "Refresh ARP cache")
    public ResponseEntity<ApiResponse<Void>> applyPortForwarding() {
        deviceService.refreshArp();
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Port forwarding triggered in background"
        ));
    }
}
