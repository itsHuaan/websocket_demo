package com.example.websocket_demo.enumeration;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

/**
 * Phone country codes the backend can accept at sign-up / sign-in. Currently only
 * Vietnam is supported; add a constant here to offer another country in the picker.
 * {@code isoCode} is the ISO 3166-1 alpha-2 code the frontend turns into a flag.
 */
@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum PhoneCountryCode {
    VN("Vietnam", "VN", "+84");

    String countryName;
    String isoCode;
    String dialCode;
}
