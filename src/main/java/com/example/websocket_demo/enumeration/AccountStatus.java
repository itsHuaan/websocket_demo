package com.example.websocket_demo.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AccountStatus {
    ACTIVE(1),
    INACTIVE(2),
    SUSPENDED(3);
    int value;
}
