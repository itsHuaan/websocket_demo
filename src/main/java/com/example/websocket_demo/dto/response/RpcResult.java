package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.ARRAY)
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonDeserialize(using = RpcResultDeserializer.class)
public class RpcResult<T> {
    Integer status;
    T payload;
}
