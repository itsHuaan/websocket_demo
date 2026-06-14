package com.example.websocket_demo.dto.response;

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
    String firstName;
    String lastName;
    String type;
    String token;
    String refreshToken;
    String profilePicture;
    String role;

    public SignInResponse(Long id, String username, String firstName, String lastName, String token, String refreshToken, String profilePicture, String role) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = "Bearer";
        this.token = token;
        this.refreshToken = refreshToken;
        this.profilePicture = profilePicture;
        this.role = role;
    }
}

