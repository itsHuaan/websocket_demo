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
public class ResetPasswordRequest {
    @Schema(example = "your_name@gmail.com")
    @NonNull
    String email;
    @Schema(example = "123456")
    @NonNull
    String otp;
    @Schema(example = "your_new_password")
    @NonNull
    String newPassword;
}
