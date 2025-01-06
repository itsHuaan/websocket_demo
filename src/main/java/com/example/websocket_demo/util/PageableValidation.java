package com.example.websocket_demo.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class PageableValidation {
    public static Pageable setDefault(Integer page, Integer size) {
        if (size == null || page == null) {
            return Pageable.unpaged();
        }
        if (size > 0 && page >= 0) {
            return PageRequest.of(page, size);
        }
        return Pageable.unpaged();
    }
}
