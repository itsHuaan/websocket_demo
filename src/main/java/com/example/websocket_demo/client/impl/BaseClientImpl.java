package com.example.websocket_demo.client.impl;

import com.example.websocket_demo.client.BaseClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

@Slf4j
@Component
public class BaseClientImpl implements BaseClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    public Map<String, String> fetchPhoneCodes() throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://country.io/phone.json"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Response: {}", response.body());

        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new IllegalStateException("Failed to fetch phone codes: HTTP " + response.statusCode());
        }

        Map<String, String> codes = MAPPER.readValue(response.body(), new TypeReference<>() {});

        codes.values().removeIf(value -> value == null || value.isBlank());

        return codes;
    }
}
