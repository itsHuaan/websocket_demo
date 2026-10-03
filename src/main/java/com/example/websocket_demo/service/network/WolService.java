package com.example.websocket_demo.service.network;

import com.example.websocket_demo.dto.request.WolRequest;

public interface WolService {
    void wakeOnLan(String macAddress, String host, int port);
}
