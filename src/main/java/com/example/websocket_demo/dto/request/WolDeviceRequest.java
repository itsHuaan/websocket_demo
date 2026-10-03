package com.example.websocket_demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class WolDeviceRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "MAC address is required")
    @Pattern(regexp = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})$", message = "Invalid MAC address format")
    private String macAddress;

    private String host = "255.255.255.255";

    private int port = 9;
}
