package com.example.websocket_demo.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.util.Const;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@Tag(name = "Test Controller")
@RequestMapping(value = Const.API_PREFIX + "/test")
public class TestController {

    @Operation(summary = "This is my first test controller")
    @GetMapping("/first")
    public ResponseEntity<?> firstTestMethod() {
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse(
                        HttpStatus.OK,
                        "This controller for testing",
                        null));
    }

}
