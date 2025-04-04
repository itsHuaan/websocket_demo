package com.example.websocket_demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SignInResponse {
    Long id;
    String username;
    String type;
    String token;

    public SignInResponse(Long id, String username, String token) {
        this.id = id;
        this.username = username;
        this.type = "Bearer";
        this.token = token;
    }
}
