package com.example.websocket_demo.common;

import com.example.websocket_demo.enumeration.VietnamPhoneFormat;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.objectweb.asm.TypeReference;

import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.example.websocket_demo.enumeration.VietnamPhoneFormat.*;

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

    public static <K1, V1, K2, V2> Map<K2, V2> convertMap(Map<K1, V1> originalMap, Function<K1, K2> keyMapper, Function<V1, V2> valueMapper) {
        if (originalMap == null) {
            return new HashMap<>();
        }
        return originalMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> keyMapper.apply(e.getKey()),
                        e -> valueMapper.apply(e.getValue())
                ));
    }

    public static boolean isNullOrZero(Object obj) {
        if (obj == null) return true;
        if (obj instanceof Number) return ((Number) obj).doubleValue() == 0.0;
        return false;
    }

    public static boolean isNullOrEmpty(Object obj) {
        switch (obj) {
            case null -> {
                return true;
            }
            case String s -> {
                return s.trim().isEmpty();
            }
            case Collection<?> c -> {
                return c.isEmpty();
            }
            case Map<?, ?> m -> {
                return m.isEmpty();
            }
            case Optional<?> o -> {
                return o.isEmpty();
            }
            default -> {
            }
        }
        if (obj.getClass().isArray()) return java.lang.reflect.Array.getLength(obj) == 0;
        return false;
    }

    public static String formatVnPhone(String phone, VietnamPhoneFormat targetFormat) {
        if (phone == null || !phone.matches(Const.VN_PHONE_REGEX)) {
            throw new IllegalArgumentException("Invalid Vietnamese phone number");
        }

        String coreNumber = phone.replaceAll("^(?:\\+?84|0)", ISDN.getValue());

        return switch (targetFormat) {
            case ZERO -> ZERO.getValue() + coreNumber;
            case MSISDN -> MSISDN.getValue() + coreNumber;
            case PLUS_MSISDN -> PLUS_MSISDN.getValue() + coreNumber;
            case ISDN -> coreNumber;
        };
    }

    public static String toSnakeCase(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        String temp = input.replace("Đ", "D").replace("đ", "d");
        temp = Normalizer.normalize(temp, Normalizer.Form.NFD);
        temp = Pattern.compile("\\p{InCombiningDiacriticalMarks}+").matcher(temp).replaceAll("");

        temp = temp.replaceAll("([a-z])([A-Z]+)", "$1_$2");

        return temp.toLowerCase()
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
    }
}
