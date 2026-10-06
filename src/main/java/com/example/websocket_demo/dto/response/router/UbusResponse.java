package com.example.websocket_demo.dto.response.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UbusResponse {
    private String jsonrpc;
    private Integer id;
    private List<Object> result;
}
