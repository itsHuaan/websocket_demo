package com.example.websocket_demo.service.otp;

public interface IOtpService {
    void generateAndStoreOtp(String email);

    String getOtp(String email);
}
