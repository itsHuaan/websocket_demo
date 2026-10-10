package com.example.websocket_demo.service.network;

import com.example.websocket_demo.dto.request.RouterLoginRequest;
import com.example.websocket_demo.dto.response.ArpEntryResponse;

import java.util.List;

public interface RouterService {
    boolean login(RouterLoginRequest credentials);
    boolean applyPortForwarding();
    List<ArpEntryResponse> getArpTable();
}
