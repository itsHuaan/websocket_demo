package com.example.websocket_demo.service.otp;

import com.example.websocket_demo.dto.response.ApiResponse;

public interface OtpService {
    ApiResponse<String> generateAndStoreOtp(String email);

    String getOtp(String email);

    boolean isOtpUsed(String email);

    void markOtpAsUsed(String email);
}

