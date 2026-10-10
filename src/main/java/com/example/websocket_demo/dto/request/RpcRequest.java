package com.example.websocket_demo.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RpcRequest<T> {
    @Builder.Default
    private String jsonrpc = "2.0";
    private String id;
    private String method;
    private T params;
}
