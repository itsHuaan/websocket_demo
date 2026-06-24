package com.example.websocket_demo.service.otp.impl;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.dto.otp.OtpData;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.otp.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class OtpServiceImpl implements OtpService {
    StringRedisTemplate redisTemplate;

    @Override
    public ApiResponse<String> generateAndStoreOtp(String email) {
        SecureRandom secureRandom = new SecureRandom();
        StringBuilder otp = new StringBuilder(Const.OTP_LENGTH);
        String key = "otp:" + email;
        for (int i = 0; i < Const.OTP_LENGTH; i++) {
            int index = secureRandom.nextInt(Const.SALT_CHARS.length());
            otp.append(Const.SALT_CHARS.charAt(index));
        }

        OtpData otpData = OtpData.builder()
                .code(otp.toString())
                .isUsed(false)
                .build();

        redisTemplate.opsForValue().set(key, Objects.requireNonNull(DataUtil.parseObjectToJson(otpData)), 3, TimeUnit.MINUTES);

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
                long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (expire > 0) {
                    redisTemplate.opsForValue().set(key, Objects.requireNonNull(DataUtil.parseObjectToJson(otpData)), expire, TimeUnit.SECONDS);
                } else {
                    redisTemplate.opsForValue().set(key, Objects.requireNonNull(DataUtil.parseObjectToJson(otpData)), 3, TimeUnit.MINUTES);
                }
            }
        }
    }
}

