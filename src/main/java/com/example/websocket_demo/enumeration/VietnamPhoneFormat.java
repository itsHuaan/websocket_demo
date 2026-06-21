package com.example.websocket_demo.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum VietnamPhoneFormat {
    ZERO("0"),
    MSISDN("84"),
    PLUS_MSISDN("+84"),
    ISDN("");

    String value;
}
