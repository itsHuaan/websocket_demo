package com.example.websocket_demo.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_EMPTY)
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
    String previousUsername;
    LocalDateTime lastUsernameChangeDate;

    public SignInResponse(Long id, String username, String firstName, String lastName, String token, String refreshToken, String profilePicture, String role, String previousUsername, java.time.LocalDateTime lastUsernameChangeDate) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.type = "Bearer";
        this.token = token;
        this.refreshToken = refreshToken;
        this.profilePicture = profilePicture;
        this.role = role;
        this.previousUsername = previousUsername;
        this.lastUsernameChangeDate = lastUsernameChangeDate;
    }
}

