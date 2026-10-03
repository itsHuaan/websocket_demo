package com.example.websocket_demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WolDeviceResponse {
    private Long id;
    private String name;
    private String macAddress;
    private String host;
    private Integer port;
    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;
}
