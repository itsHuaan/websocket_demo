package com.example.websocket_demo.client;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class GenericRestClient {
    private final RestTemplate restTemplate;
    public <T, R> ResponseEntity<R> executeApiCall(
            String url,
            HttpMethod method,
            Map<String, String> queryParams,
            HttpHeaders headers,
            T body,
            ParameterizedTypeReference<R> responseType) {

        if (headers == null) {
            headers = new HttpHeaders();
        }

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(url);
        if (queryParams != null && !queryParams.isEmpty()) {
            queryParams.forEach(uriBuilder::queryParam);
        }

        HttpEntity<T> requestEntity = new HttpEntity<>(body, headers);

        try {
            return restTemplate.exchange(
                    uriBuilder.toUriString(),
                    method,
                    requestEntity,
                    responseType
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
