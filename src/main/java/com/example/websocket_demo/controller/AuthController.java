package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.model.SignUpRequest;
import com.example.websocket_demo.service.auth.IAuthService;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Authentication Controller")
@RequestMapping(value = Const.API_PREFIX + "/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    IAuthService authService;

    @Operation(summary = "Sign users in")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody SignInRequest credentials) {
        ApiResponse<?> response = authService.signIn(credentials);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Sign users up")
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SignUpRequest credentials) {
        ApiResponse<?> response = authService.signUp(credentials);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
