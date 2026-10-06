package com.example.websocket_demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArpEntryResponse {
    private String device;
    private String macAddress;
    private String ipAddress;
}
