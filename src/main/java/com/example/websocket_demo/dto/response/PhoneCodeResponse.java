package com.example.websocket_demo.dto.response;

/**
 * A supported phone country code offered in the sign-up picker.
 * {@code isoCode} is rendered as a flag on the frontend; {@code dialCode} is the
 * international prefix (e.g. "+84").
 */
public record PhoneCodeResponse(String country, String isoCode, String dialCode) {
}
