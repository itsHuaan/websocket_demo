package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.WolDeviceResponse;
import com.example.websocket_demo.service.network.WolDeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/network/devices")
@RequiredArgsConstructor
@Tag(name = "Network Devices API", description = "CRUD operations for Wake on LAN devices")
public class WolDeviceController {

    private final WolDeviceService deviceService;

    @PostMapping
    @Operation(summary = "Create a new device")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> createDevice(@Valid @RequestBody WolDeviceRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.CREATED,
                "Device created successfully",
                deviceService.createDevice(request)
        ));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing device")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> updateDevice(
            @PathVariable Long id, 
            @Valid @RequestBody WolDeviceRequest request) {
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Device updated successfully",
                deviceService.updateDevice(id, request)
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a device")
    public ResponseEntity<ApiResponse<Void>> deleteDevice(@PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Device deleted successfully"
        ));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a device by ID")
    public ResponseEntity<ApiResponse<WolDeviceResponse>> getDevice(@PathVariable Long id) {
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Success",
                deviceService.getDevice(id)
        ));
    }

    @GetMapping
    @Operation(summary = "Get all devices")
    public ResponseEntity<ApiResponse<List<WolDeviceResponse>>> getAllDevices() {
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Success",
                deviceService.getAllDevices()
        ));
    }

    @PostMapping("/{id}/wake")
    @Operation(summary = "Wake up a specific device by ID")
    public ResponseEntity<ApiResponse<String>> wakeDevice(@PathVariable Long id) {
        deviceService.wakeDevice(id);
        return ResponseEntity.ok(new ApiResponse<>(
                HttpStatus.OK,
                "Wake on LAN packet sent successfully",
                "Device ID: " + id
        ));
    }
}
