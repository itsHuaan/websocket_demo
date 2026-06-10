package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;
import com.example.websocket_demo.service.auth.IAuthService;
import com.example.websocket_demo.common.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.websocket_demo.dto.request.ForgotPasswordRequest;
import com.example.websocket_demo.dto.request.ResetPasswordRequest;
import com.example.websocket_demo.dto.request.VerifyOtpRequest;
import com.example.websocket_demo.dto.request.ResendOtpRequest;

@RestController
@Tag(name = "Authentication Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    IAuthService authService;

    @Operation(summary = "Sign users in")
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@RequestBody SignInRequest credentials) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "You're now logged in", authService.signIn(credentials)));
    }

    @Operation(summary = "Sign users up")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody SignUpRequest credentials) {
        authService.signUp(credentials);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, "Sign up successful, please check your email for OTP"));
    }

    @Operation(summary = "Verify sign up OTP")
    @PostMapping("/verify-sign-up")
    public ResponseEntity<ApiResponse<?>> verifySignUp(@RequestBody VerifyOtpRequest request) {
        authService.verifySignUp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "Account activated successfully"));
    }

    @Operation(summary = "Resend sign up OTP")
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<?>> resendOtp(@RequestBody ResendOtpRequest request) {
        authService.resendSignUpOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "A new OTP has been sent to your email"));
    }

    @Operation(summary = "Request password reset OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "Password reset OTP sent to your email"));
    }

    @Operation(summary = "Reset password with OTP")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, "Password has been reset successfully"));
    }
}

