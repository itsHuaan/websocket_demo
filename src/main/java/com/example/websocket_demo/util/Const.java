package com.example.websocket_demo.util;

import java.util.regex.Pattern;

public class Const {
    public static final String API_PREFIX = "/v1/api";
    public static final String emailRegex = "^[\\w!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(emailRegex);
}
