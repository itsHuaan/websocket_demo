package com.example.websocket_demo.dto;

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
public class UserDto {
    Long userId;
    String email;
    String username;
    String profilePicture;
    String status;
    LocalDateTime createdAt;
    LocalDateTime modifiedAt;
    LocalDateTime deletedAt;
}
