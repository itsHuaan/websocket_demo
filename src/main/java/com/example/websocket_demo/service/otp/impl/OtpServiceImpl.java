package com.example.websocket_demo.service.otp.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.dto.otp.OtpData;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.otp.IOtpService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
        String key = "otp:" + email;
        for (int i = 0; i < OTP_LENGTH; i++) {
            int index = secureRandom.nextInt(SALT_CHARS.length());
            otp.append(SALT_CHARS.charAt(index));
        }

        OtpData otpData = OtpData.builder()
                .code(otp.toString())
                .isUsed(false)
                .build();

        redisTemplate.opsForValue().set(key, DataUtil.parseObjectToJson(otpData), 3, TimeUnit.MINUTES);

        return new ApiResponse<>(HttpStatus.CREATED, "OTP generated successfully", otp.toString());
    }

    @Override
    public String getOtp(String email) {
        String key = "otp:" + email;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return null;
        OtpData otpData = DataUtil.parseJsonToObject(json, OtpData.class);
        return otpData != null ? otpData.getCode() : null;
    }

    @Override
    public boolean isOtpUsed(String email) {
        String key = "otp:" + email;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) return false;
        OtpData otpData = DataUtil.parseJsonToObject(json, OtpData.class);
        return otpData != null && otpData.isUsed();
    }

    @Override
    public void markOtpAsUsed(String email) {
        String key = "otp:" + email;
        String json = redisTemplate.opsForValue().get(key);
        if (json != null) {
            OtpData otpData = DataUtil.parseJsonToObject(json, OtpData.class);
            if (otpData != null) {
                otpData.setUsed(true);
                Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (expire != null && expire > 0) {
                    redisTemplate.opsForValue().set(key, DataUtil.parseObjectToJson(otpData), expire, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, DataUtil.parseObjectToJson(otpData), 3, TimeUnit.MINUTES);
                }
            }
        }
    }
}

