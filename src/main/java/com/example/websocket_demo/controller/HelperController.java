package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.Const;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.service.helper.IHelperService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Tag(name = "Helper Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/helper")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class HelperController {
    IHelperService helperService;

    private <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, message, data));
    }

    @Operation(summary = "Get all phone number code")
    @GetMapping("/phone-codes")
    public ResponseEntity<ApiResponse<Map<Object, Object>>> getPhoneCodes() {
        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponse<>(HttpStatus.OK, "Codes fetched", helperService.getAllPhoneCodes()));
    }
}
