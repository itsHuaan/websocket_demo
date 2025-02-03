package com.example.websocket_demo.model;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductRequest {
    String productName;
    Long userId;
    MultipartFile[] media;
    List<OptionRequest<?>> options;
    List<SkuRequest> skus;

    @Data
    @NoArgsConstructor
    public static class OptionRequest<T> {
        String name;
        List<T> values;
    }

    @Data
    @NoArgsConstructor
    public static class SkuRequest {
        Double price;
        List<SkuValueRequest> values;

        @Data
        @NoArgsConstructor
        public static class SkuValueRequest {
            String option;
            String value;
        }
    }
}
