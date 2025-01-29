package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.service.product.IProductService;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Product Controller")
@RequestMapping(value = Const.API_PREFIX + "/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;

    @Operation(summary = "Add a product")
    @PostMapping
    public ResponseEntity<?> addProduct(@RequestBody ProductRequest request) {
        ApiResponse<?> response = productService.addProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @Operation(summary = "Get all product")
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAll());
    }
}
