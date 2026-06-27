package com.example.websocket_demo.client.impl;

import com.example.websocket_demo.client.BaseClient;
import com.example.websocket_demo.common.DataUtil;
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
import java.util.TreeMap;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.FAILED_TO_FETCH_PHONE_CODES;

@Slf4j
@Component
public class BaseClientImpl implements BaseClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private final MessageService messageService;

    public BaseClientImpl(MessageService messageService) {
        this.messageService = messageService;
    }

    @Override
    public Map<String, String> fetchPhoneCodes() throws Exception {
        String url = "https://country.io/phone.json";

        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        log.info("Response from {}: {}", url, response.body().trim());

        if (response.statusCode() != HttpStatus.OK.value()) {
            throw new IllegalStateException(messageService.getMessage(FAILED_TO_FETCH_PHONE_CODES.getCode()) + " " + response.statusCode());
        }

        Map<String, String> codes = MAPPER.readValue(response.body(), new TypeReference<>() {
        });

        codes.values().removeIf(DataUtil::isNullOrEmpty);

        return new TreeMap<>(codes);
    }
}
