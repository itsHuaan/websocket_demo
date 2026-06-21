package com.example.websocket_demo.client;

import java.util.Map;

public interface BaseClient {
    Map<String, String> fetchPhoneCodes() throws Exception;
}
