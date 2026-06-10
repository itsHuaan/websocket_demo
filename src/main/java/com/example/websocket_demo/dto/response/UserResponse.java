package com.example.websocket_demo.dto.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {
    Long userId;
    String email;
    String firstName;
    String lastName;
    String username;
    String profilePicture;
    String role;
    String status;
    String createdAt;
    String modifiedAt;
    String deletedAt;
}

