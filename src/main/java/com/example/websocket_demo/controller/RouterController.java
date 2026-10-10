package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.request.RouterLoginRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.network.RouterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/router")
@RequiredArgsConstructor
@Tag(name = "Router API", description = "Communicate with router")
public class RouterController {
    private final RouterService routerService;

    @PostMapping("/login")
    @Operation(summary = "Login to the router")
    public ResponseEntity<ApiResponse<?>> routerLogin(@RequestBody RouterLoginRequest credentials) {
        boolean success = routerService.login(credentials);
        return ResponseEntity.ok(new ApiResponse<>(
                success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR,
                success ? "Login successfully" : "Failed to login"
        ));
    }
}
