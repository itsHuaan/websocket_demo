package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.WolRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.network.WolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/network")
@RequiredArgsConstructor
@Tag(name = "Network API", description = "Endpoints for network utilities")
public class WolController {

    private final WolService wolService;

    @PostMapping("/wol")
    @Operation(summary = "Send Wake on LAN packet", description = "Sends a magic packet to wake up a computer on the network")
    public ResponseEntity<ApiResponse<String>> wakeOnLan(@Valid @RequestBody WolRequest request) {
        wolService.wakeOnLan(request);
        return ResponseEntity.ok(new ApiResponse<>(
                org.springframework.http.HttpStatus.OK,
                "Wake on LAN packet sent successfully",
                "MAC: " + request.getMacAddress()
        ));
    }
}
