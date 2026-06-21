package com.example.websocket_demo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import static com.example.websocket_demo.common.Const.EMAIL_REGEX;
import static com.example.websocket_demo.common.Const.VN_PHONE_REGEX;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SignUpRequest {
    @Pattern(regexp = EMAIL_REGEX, message = "Invalid email format")
    @Schema(example = "your_name@gmail.com", pattern = EMAIL_REGEX)
    @NonNull
    String email;
    @Schema(example = "your_username")
    @NonNull
    String username;
    @Pattern(regexp = VN_PHONE_REGEX, message = "Invalid Vietnamese phone number")
    @Schema(description = "Optional Vietnamese phone number; can be used to sign in later", example = "0912345678")
    String phoneNumber;
    @Schema(example = "your first name")
    String firstName;
    @Schema(example = "your last name")
    String lastName;
    @Schema(example = "your_password")
    @NonNull
    String password;
}

