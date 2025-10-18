package com.example.websocket_demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.time.ZoneId;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    int code;
    String message;
    T data;
    long timestamp;

    public ApiResponse(HttpStatus code, String message, T data) {
        this(code, message, data, LocalDateTime.now());
    }

    public ApiResponse(HttpStatus code, String message) {
        this(code, message, null, LocalDateTime.now());
    }

    public ApiResponse(HttpStatus code, String message, T data, LocalDateTime timestamp) {
        this.code = code.value();
        this.message = message;
        this.data = data;
        this.timestamp = timestamp.atZone(ZoneId.systemDefault()).toEpochSecond();
    }
}
