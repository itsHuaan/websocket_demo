package com.example.websocket_demo.common;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.Field;

public class DataUtil {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static boolean hasNullField(Object obj) {
        if (obj == null) {
            throw new IllegalArgumentException("The provided object is null.");
        }

        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);
            try {
                if (field.get(obj) == null) {
                    return true;
                }
            } catch (IllegalAccessException e) {
                return false;
            }
        }
        return false;
    }

    public static String parseObjectToJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (Exception e) {
            return null;
        }
    }

    public static <T> T parseJsonToObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isNullOrZero(Object obj) {
        if (obj == null) return true;
        if (obj instanceof Number) return ((Number) obj).doubleValue() == 0.0;
        return false;
    }
}
