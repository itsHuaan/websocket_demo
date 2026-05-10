package com.example.websocket_demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignInRequest {
    @Schema(example = "your_username")
    @NonNull
    String username;
    @Schema(example = "your_password")
    @NonNull
    String password;
}

