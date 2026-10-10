package com.example.websocket_demo.client.impl;

import com.example.websocket_demo.client.GenericRestClient;
import com.example.websocket_demo.client.RouterClient;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.common.DateUtil;
import com.example.websocket_demo.dto.request.RouterLoginRequest;
import com.example.websocket_demo.dto.request.RpcRequest;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.dto.response.RpcResponse;
import com.example.websocket_demo.dto.response.router.ArpTableData;
import com.example.websocket_demo.dto.response.router.LoginData;
import com.example.websocket_demo.dto.response.router.PortForwardingData;
import com.example.websocket_demo.dto.response.router.UbusResponse;
import com.example.websocket_demo.service.redis.RedisService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class RouterClientImpl implements RouterClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final RedisService redisService;
    private final GenericRestClient restTemplate;

    private static final String UBUS_SESSION_REDIS_KEY = "router:ubus_rpc_session";
    @Value("${router.host:http://localhost}")
    private String host;

    @Value("${router.username:admin}")
    private String username;

    @Value("${router.password:admin}")
    private String password;

    @Value("${router.ubus:/ubus}")
    private String ubus;

    private String getSessionToken() {
        String token = redisService.getString(UBUS_SESSION_REDIS_KEY);
        return !DataUtil.isNullOrEmpty(token)
                ? token
                : login(RouterLoginRequest.builder()
                .username(username)
                .password(password)
                .build()).getUbusRpcSession();
    }

    @Override
    public LoginData login(RouterLoginRequest credentials) {
        String url = host + ubus;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            RpcRequest<List<Object>> payload = RpcRequest.<List<Object>>builder()
                    .id(DateUtil.formatDate(LocalDateTime.now(), "yyyyMMddHHmmssSSS"))
                    .method("call")
                    .params(List.of("00000000000000000000000000000000",
                            "session",
                            "login",
                            credentials))
                    .build();

            ResponseEntity<RpcResponse<LoginData>> response = restTemplate.executeApiCall(
                    url,
                    HttpMethod.POST,
                    null,
                    headers,
                    payload,
                    new ParameterizedTypeReference<>() {
                    }
            );

            RpcResponse<LoginData> responseBody = response.getBody();

            if (responseBody == null || responseBody.getResult() == null || responseBody.getResult().getStatus() != 0) {
                throw new RuntimeException("Invalid response from router login");
            }

            LoginData loginData = responseBody.getResult().getPayload();
            String token = loginData.getUbusRpcSession();
            Long timeout = loginData.getTimeout() != null ? loginData.getTimeout() : 300L;

            if (DataUtil.isNullOrEmpty(token)) {
                return null;
            }
            redisService.setString(UBUS_SESSION_REDIS_KEY, token, timeout);
            log.info("Router login completed successfully.");
            return loginData;
        } catch (Exception e) {
            log.error("Error during router login", e);
            throw new RuntimeException("Failed to login to router", e);
        }
    }

    @Override
    public boolean applyPortForwarding() {
        String token = getSessionToken();
        String url = host + ubus;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            Map<String, Object> redirectParams = new HashMap<>();
            redirectParams.put("redirect", new Object[]{});

            RpcRequest<Object[]> payload = RpcRequest.<Object[]>builder()
                    .id(DateUtil.formatDate(LocalDateTime.now(), "yyyyMMddHHmmssSSS"))
                    .method("call")
                    .params(new Object[]{
                            token,
                            "data_repo.weboui",
                            "portfwd_set",
                            redirectParams
                    })
                    .build();

            ResponseEntity<RpcResponse<PortForwardingData>> response = restTemplate.executeApiCall(
                    url,
                    HttpMethod.POST,
                    null,
                    headers,
                    payload,
                    new ParameterizedTypeReference<>() {}
            );

            RpcResponse<PortForwardingData> responseBody = response.getBody();
            if (responseBody == null || responseBody.getResult() == null || responseBody.getResult().getStatus() != 0) {
                log.error("Invalid response from router port forwarding");
                return false;
            }

            PortForwardingData pfData = responseBody.getResult().getPayload();
            if (pfData == null || !Boolean.TRUE.equals(pfData.getStatus())) {
                log.error("Port forwarding failed: status is not true");
                return false;
            }

            log.info("Router apply port forwarding completed successfully.");
            return true;

        } catch (Exception e) {
            log.error("Error during applyPortForwarding", e);
            return false;
        }
    }

    @Override
    public List<ArpEntryResponse> getArpTable() {
        String token = getSessionToken();
        String url = host + ubus;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Referer", host);

            RpcRequest<Object[]> payload = RpcRequest.<Object[]>builder()
                    .id(DateUtil.formatDate(LocalDateTime.now(), "yyyyMMddHHmmssSSS"))
                    .method("call")
                    .params(new Object[]{
                            token,
                            "oui.network",
                            "arp_table",
                            new HashMap<>()
                    })
                    .build();

            ResponseEntity<RpcResponse<ArpTableData>> response = restTemplate.executeApiCall(
                    url,
                    HttpMethod.POST,
                    null,
                    headers,
                    payload,
                    new ParameterizedTypeReference<>() {}
            );

            RpcResponse<ArpTableData> responseBody = response.getBody();
            if (responseBody == null || responseBody.getResult() == null || responseBody.getResult().getStatus() != 0) {
                throw new RuntimeException("Invalid response from router getArpTable");
            }

            ArpTableData arpData = responseBody.getResult().getPayload();

            List<ArpEntryResponse> arpEntries = new ArrayList<>();
            if (arpData != null && arpData.getEntries() != null) {
                for (ArpTableData.ArpEntry entry : arpData.getEntries()) {
                    arpEntries.add(ArpEntryResponse.builder()
                            .device(entry.getDevice())
                            .macAddress(entry.getMacaddr())
                            .ipAddress(entry.getIpaddr())
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
