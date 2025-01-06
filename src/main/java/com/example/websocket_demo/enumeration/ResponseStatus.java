package com.example.websocket_demo.enumeration;

import org.springframework.http.HttpStatus;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ResponseStatus {
    CREATED(1001, "", HttpStatus.CREATED),
    FAILURE(1002, "", HttpStatus.BAD_REQUEST);

    Integer status;
    String message;
    HttpStatus httpStatus;
}
