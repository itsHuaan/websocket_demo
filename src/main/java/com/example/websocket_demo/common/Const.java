package com.example.websocket_demo.common;

import java.util.List;
import java.util.regex.Pattern;

public class Const {
    public static class DateFormat {
        public static final String HHmmss_MMMddyyyy = "HH:mm:ss MMM dd, yyyy";
        public static final String ddMMyyyy_HHmmss = "dd/MM/yyyy HH:mm:ss";
        public static final String ddMMyyyy = "dd/MM/yyyy";
        public static final String MMMddyyyy_HHmmss = "MMM dd, yyyy HH:mm:ss";
        public static final String MMMddyyyy = "MMM dd, yyyy";
        public static final String yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";
        public static final String yyyyMMdd = "yyyy-MM-dd";
    }

    public static final List<String> PHONE_NUMBER_CODE = List.of("+84");
    public static final String API_PREFIX_V1 = "/v1/api";
    public static final String SALT_CHARS = "1234567890";
    public static final int OTP_LENGTH = 6;
    public static final String EMAIL_REGEX = "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}$";
    public static final String VN_PHONE_REGEX = "^(?:\\+?84|0)?[35789]\\d{8}$";
    public static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
}
