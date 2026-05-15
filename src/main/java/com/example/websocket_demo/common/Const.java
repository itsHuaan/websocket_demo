package com.example.websocket_demo.common;

import java.util.regex.Pattern;

public class Const {
    public class DateFormat {
        public static final String HHmmss_MMMddyyyy = "HH:mm:ss MMM dd, yyyy";
        public static final String ddMMyyyy_HHmmss = "dd/MM/yyyy HH:mm:ss";
        public static final String ddMMyyyy = "dd/MM/yyyy";
        public static final String MMMddyyyy_HHmmss = "MMM dd, yyyy HH:mm:ss";
        public static final String MMMddyyyy = "MMM dd, yyyy";
        public static final String yyyyMMdd_HHmmss = "yyyy-MM-dd HH:mm:ss";
        public static final String yyyyMMdd = "yyyy-MM-dd";
    }

    public static final String API_PREFIX_V1 = "/v1/api";
    public static final String SALT_CHARS = "1234567890";
    public static final String emailRegex = "^[\\w!#$%&amp;'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&amp;'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$";
    public static final int OTP_LENGTH = 6;
    public static final Pattern EMAIL_PATTERN = Pattern.compile(emailRegex);
}
