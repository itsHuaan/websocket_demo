package com.example.websocket_demo.service.network;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.WolDeviceResponse;

import java.util.List;

public interface WolDeviceService {
    WolDeviceResponse createDevice(WolDeviceRequest request);
    WolDeviceResponse updateDevice(Long id, WolDeviceRequest request);
    void deleteDevice(Long id);
    WolDeviceResponse getDevice(Long id);
    List<WolDeviceResponse> getAllDevices();
    void wakeDevice(Long id);
}
