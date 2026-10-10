package com.example.websocket_demo.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RpcResponse<T> {
    String jsonrpc;
    Long id;
    RpcResult<T> result;
}
