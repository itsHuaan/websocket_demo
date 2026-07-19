package com.example.websocket_demo.dto.request.score;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserScoreRequest {
    String name;
    String quiz;
    Integer correctAnswers;
    Integer incorrectAnswers;
}
