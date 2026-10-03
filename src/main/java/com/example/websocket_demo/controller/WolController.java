package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.WolRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.network.WolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/network")
@RequiredArgsConstructor
@Tag(name = "Network API", description = "Endpoints for network utilities")
public class WolController {

    private final WolService wolService;

    @GetMapping("/wol")
    @Operation(summary = "Send Wake on LAN packet", description = "Sends a magic packet to wake up a computer on the network")
    @Validated
    public ResponseEntity<ApiResponse<String>> wakeOnLan(@RequestParam(name = "mac-address")
                                                         @NotBlank(message = "MAC address is required")
                                                         @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format")
                                                         String macAddress,
                                                         String host,
                                                         int port) {
        wolService.wakeOnLan(macAddress, host, port);
        return ResponseEntity.ok(new ApiResponse<>(
                org.springframework.http.HttpStatus.OK,
                "Wake on LAN packet sent successfully",
                "MAC: " + macAddress
        ));
    }
}
