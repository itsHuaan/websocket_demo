package com.example.websocket_demo.dto.response.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginData {
    @JsonProperty("ubus_rpc_session")
    private String ubusRpcSession;
    private Long timeout;
}

