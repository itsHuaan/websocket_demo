package com.example.websocket_demo.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;

@Data
@NoArgsConstructor
public class ProductRequest {
    private String productName;
    private Long userId;
    private List<OptionRequest<?>> options;
    private List<SkuRequest> skus;

    @Data
    @NoArgsConstructor
    public static class OptionRequest<T> {
        private String optionName;
        private List<T> values;
    }

    @Data
    @NoArgsConstructor
    public static class SkuRequest {
        private Double price;
        private List<SkuValueRequest> skuValues;

        @Data
        @NoArgsConstructor
        public static class SkuValueRequest {
            private String optionName;
            private String valueName;
        }
    }
}
