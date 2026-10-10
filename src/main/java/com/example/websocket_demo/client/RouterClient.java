package com.example.websocket_demo.client;

import com.example.websocket_demo.dto.request.RouterLoginRequest;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.dto.response.router.LoginData;

import java.util.List;

public interface RouterClient {
    LoginData login(RouterLoginRequest credentials);
    boolean applyPortForwarding();
    List<ArpEntryResponse> getArpTable();
}
