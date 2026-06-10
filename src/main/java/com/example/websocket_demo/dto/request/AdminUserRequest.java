package com.example.websocket_demo.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

/**
 * Admin-side user edit (JSON). All fields optional — only non-null/non-blank values are applied.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserRequest {
    String firstName;
    String lastName;
    String username;
    String email;
    String password; // optional reset — applied only when non-blank
    Long roleId;
    Integer status;
}
