package com.example.websocket_demo.dto.response.router;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.Map;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class LoginData {
    @JsonProperty("ubus_rpc_session")
    String ubusRpcSession;
    Long timeout;
    Map<String, Object> acls;
    Map<String, String> data;
}

