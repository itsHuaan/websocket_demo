package com.example.websocket_demo.service.helper.impl;

import com.example.websocket_demo.client.BaseClient;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.service.helper.IHelperService;
import com.example.websocket_demo.service.redis.RedisService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HelperServiceImpl implements IHelperService {
    RedisService redisService;
    BaseClient baseClient;

    @NonFinal
    @Value(value = "${spring.data.redis.phone-codes-ttl-seconds}")
    long phoneCodesCacheTtl;

    @Override
    public Map<Object, Object> getAllPhoneCodes() {
        String phoneCodesKey = "phone_codes";
        if (!redisService.exists(phoneCodesKey)) {
            try {
                Map<String, String> phoneCodes = baseClient.fetchPhoneCodes();
                redisService.hSet(phoneCodesKey, phoneCodes, phoneCodesCacheTtl);
                return DataUtil.convertMap(phoneCodes, key -> key, val -> val);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return redisService.getHash(phoneCodesKey);
    }
}
