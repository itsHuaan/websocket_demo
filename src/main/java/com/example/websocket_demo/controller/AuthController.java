package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;
import com.example.websocket_demo.service.auth.AuthService;
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
import com.example.websocket_demo.dto.request.RefreshTokenRequest;

@RestController
@Tag(name = "Authentication Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthController {
    MessageService messageService;
    AuthService authService;

    @Operation(summary = "Sign users in")
    @PostMapping("/sign-in")
    public ResponseEntity<ApiResponse<?>> signIn(@RequestBody SignInRequest credentials) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(YOU_RE_NOW_LOGGED_IN.getCode()), authService.signIn(credentials)));
    }

    @Operation(summary = "Exchange a refresh token for a new access token")
    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<?>> refreshToken(@RequestBody RefreshTokenRequest request) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(TOKEN_REFRESHED.getCode()), authService.refreshToken(request.getRefreshToken())));
    }

    @Operation(summary = "Log out and revoke the refresh token")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(@RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(LOGGED_OUT.getCode())));
    }

    @Operation(summary = "Sign users up")
    @PostMapping("/sign-up")
    public ResponseEntity<ApiResponse<?>> signUp(@RequestBody SignUpRequest credentials) {
        authService.signUp(credentials);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, messageService.getMessage(SIGN_UP_SUCCESSFUL_PLEASE_CHECK_YOUR_EMAIL_FOR_OTP.getCode())));
    }

    @Operation(summary = "Verify sign up OTP")
    @PostMapping("/verify-sign-up")
    public ResponseEntity<ApiResponse<?>> verifySignUp(@RequestBody VerifyOtpRequest request) {
        authService.verifySignUp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(ACCOUNT_ACTIVATED_SUCCESSFULLY.getCode())));
    }

    @Operation(summary = "Resend sign up OTP")
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<?>> resendOtp(@RequestBody ResendOtpRequest request) {
        authService.resendSignUpOtp(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(A_NEW_OTP_HAS_BEEN_SENT_TO_YOUR_EMAIL.getCode())));
    }

    @Operation(summary = "Request password reset OTP")
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<?>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PASSWORD_RESET_OTP_SENT_TO_YOUR_EMAIL.getCode())));
    }

    @Operation(summary = "Reset password with OTP")
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PASSWORD_HAS_BEEN_RESET_SUCCESSFULLY.getCode())));
    }
}
