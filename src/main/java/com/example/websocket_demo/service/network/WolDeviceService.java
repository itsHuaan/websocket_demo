package com.example.websocket_demo.service.network;

import com.example.websocket_demo.dto.request.WolDeviceRequest;
import com.example.websocket_demo.dto.response.ArpEntryResponse;
import com.example.websocket_demo.dto.response.WolDeviceResponse;

import java.util.List;

public interface WolDeviceService {
    WolDeviceResponse createDevice(Long userId, WolDeviceRequest request);
    WolDeviceResponse updateDevice(Long userId, Long id, WolDeviceRequest request);
    void deleteDevice(Long userId, Long id);
    WolDeviceResponse getDevice(Long userId, Long id);
    List<WolDeviceResponse> getAllDevices(Long userId);
    void wakeDevice(Long userId, Long id);
    void applyPortForwarding();
    List<ArpEntryResponse> getArpTable();
}
