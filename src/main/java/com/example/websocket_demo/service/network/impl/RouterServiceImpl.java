package com.example.websocket_demo.service.network.impl;

import com.example.websocket_demo.client.RouterClient;
import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.dto.request.RouterLoginRequest;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.service.network.RouterService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RouterServiceImpl implements RouterService {
    private final RouterClient routerClient;

    @Override
    public boolean login(RouterLoginRequest credentials) {
        return !DataUtil.isNullOrEmpty(routerClient.login(credentials));
    }

    @Override
    public boolean applyPortForwarding() {
        return routerClient.applyPortForwarding();
    }

    @Override
    public List<ArpEntryResponse> getArpTable() {
        return routerClient.getArpTable();
    }
}
