package com.example.websocket_demo.service.otp.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.service.otp.IOtpService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class OtpServiceImpl implements IOtpService {
    StringRedisTemplate redisTemplate;
    int OTP_LENGTH = 6;
    String SALT_CHARS = "0123456789";

    @Override
    public ApiResponse<String> generateAndStoreOtp(String email) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otp = new StringBuilder(OTP_LENGTH);
        String key = "otp: " + email;
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = secureRandom.nextInt(SALT_CHARS.length());
            otp.append(SALT_CHARS.charAt(index));
        }
        redisTemplate.opsForValue().set(key, otp.toString(), 3, TimeUnit.MINUTES);
        return new ApiResponse<>(HttpStatus.CREATED, "OTP generated successfully", otp.toString());
    }

    @Override
    public String getOtp(String email) {
        String key = "otp: " + email;
        return redisTemplate.opsForValue().get(key);
    }
}
