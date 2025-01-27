package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Product Controller")
@RequestMapping(value = Const.API_PREFIX + "/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    @Operation(summary = "Add a product")
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request) {
        ApiResponse<?> response = new ApiResponse<>(HttpStatus.CREATED, "Product created", request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
