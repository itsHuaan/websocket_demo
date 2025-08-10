package com.example.websocket_demo.service.otp;

import com.example.websocket_demo.dto.ApiResponse;

public interface IOtpService {
    ApiResponse<String> generateAndStoreOtp(String email);

    String getOtp(String email);
}
