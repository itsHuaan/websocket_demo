package com.example.websocket_demo.client.impl;

import com.example.websocket_demo.client.BaseClient;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.common.MessageService;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.service.redis.RedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import static com.example.websocket_demo.enumeration.ResponseMessage.FAILED_TO_FETCH_PHONE_CODES;

@Slf4j
@Component
public class BaseClientImpl implements BaseClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String UBUS_SESSION_REDIS_KEY = "router:ubus_rpc_session";

    private final MessageService messageService;
    private final RedisService redisService;
    private final RestTemplate restTemplate;

    @Value("${router.host}")
    private String host;

    @Value("${router.username}")
    private String username;

    @Value("${router.password}")
    private String password;

    @Value("${router.path}")
    private String path;

    public BaseClientImpl(MessageService messageService, RedisService redisService) {
        this.messageService = messageService;
        this.redisService = redisService;
        this.restTemplate = new RestTemplate();
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

    private String getSessionToken() {
        String token = redisService.getString(UBUS_SESSION_REDIS_KEY);
        if (!DataUtil.isNullOrEmpty(token)) {
            return token;
        }
        return login();
    }

    private String login() {
        String url = host + path;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            int randomId = new Random().nextInt(100000);

            Map<String, Object> loginParams = new HashMap<>();
            loginParams.put("username", username);
            loginParams.put("password", password);

            Map<String, Object> payload = new HashMap<>();
            payload.put("jsonrpc", "2.0");
            payload.put("id", randomId);
            payload.put("method", "call");
            payload.put("params", new Object[]{
                    "00000000000000000000000000000000",
                    "session",
                    "login",
                    loginParams
            });

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode rootNode = MAPPER.readTree(response.getBody());
            JsonNode resultNode = rootNode.path("result").path(1);
            String token = resultNode.path("ubus_rpc_session").asText();
            long timeout = resultNode.path("timeout").asLong(300);

            if (token == null || token.isEmpty()) {
                throw new RuntimeException("Failed to get session token");
            }
            log.info("Router login completed successfully.");

            // Save to redis
            redisService.setString(UBUS_SESSION_REDIS_KEY, token, timeout);

            return token;

        } catch (Exception e) {
            log.error("Error during router login", e);
            throw new RuntimeException("Failed to login to router", e);
        }
    }

    @Override
    public void applyPortForwarding() {
        String token = getSessionToken();
        String url = host + path;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            int randomId = new Random().nextInt(100000);

            Map<String, Object> redirectParams = new HashMap<>();
            redirectParams.put("redirect", new Object[]{});

            Map<String, Object> payload = new HashMap<>();
            payload.put("jsonrpc", "2.0");
            payload.put("id", randomId);
            payload.put("method", "call");
            payload.put("params", new Object[]{
                    token,
                    "data_repo.weboui",
                    "portfwd_set",
                    redirectParams
            });

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode rootNode = MAPPER.readTree(response.getBody());
            JsonNode statusNode = rootNode.path("result").path(1).path("status");

            if (statusNode.isMissingNode() || !statusNode.asBoolean()) {
                throw new RuntimeException("Port forwarding failed: result[1].status is not true");
            }

            log.info("Router apply port forwarding completed successfully.");

        } catch (Exception e) {
            log.error("Error during applyPortForwarding", e);
        }
    }

    @Override
    public List<ArpEntryResponse> getArpTable() {
        String token = getSessionToken();
        String url = host + path;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            int randomId = new Random().nextInt(100000);

            Map<String, Object> payload = new HashMap<>();
            payload.put("jsonrpc", "2.0");
            payload.put("id", randomId);
            payload.put("method", "call");
            payload.put("params", new Object[]{
                    token,
                    "oui.network",
                    "arp_table",
                    new HashMap<>()
            });

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);

            JsonNode rootNode = MAPPER.readTree(response.getBody());
            JsonNode entriesNode = rootNode.path("result").path(1).path("entries");

            List<ArpEntryResponse> arpEntries = new ArrayList<>();
            if (entriesNode.isArray()) {
                for (JsonNode entry : entriesNode) {
                    arpEntries.add(ArpEntryResponse.builder()
                            .device(entry.path("device").asText())
                            .macAddress(entry.path("macaddr").asText())
                            .ipAddress(entry.path("ipaddr").asText())
                            .build());
                }
            }

            return arpEntries;

        } catch (Exception e) {
            log.error("Error during router getArpTable", e);
            throw new RuntimeException("Failed to get ARP table", e);
        }
    }
}
