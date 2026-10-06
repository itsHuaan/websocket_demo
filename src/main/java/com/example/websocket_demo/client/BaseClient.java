package com.example.websocket_demo.client;

import com.example.websocket_demo.dto.response.ArpEntryResponse;

import java.util.List;
import java.util.Map;

public interface BaseClient {
    Map<String, String> fetchPhoneCodes() throws Exception;
    void applyPortForwarding();
    List<ArpEntryResponse> getArpTable();
}
