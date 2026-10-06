package com.example.websocket_demo.dto.response.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PortForwardingData {
    private Boolean status;
}
