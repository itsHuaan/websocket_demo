package com.example.websocket_demo.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum AccountStatus {
    ACTIVE(1, "Active"),
    INACTIVE(2, "Inactive"),
    SUSPENDED(3, "Suspended"),
    UNKNOWN(3, "Unknown");
    int value;
    String description;
}
