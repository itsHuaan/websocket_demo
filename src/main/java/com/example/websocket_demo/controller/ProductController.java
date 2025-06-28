package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.service.product.IProductService;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Product Controller")
@RequestMapping(value = Const.API_PREFIX + "/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;

    @Operation(summary = "Add a product")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addProduct(@RequestPart("product") @Valid String productJson,
                                        @RequestPart(value = "productMedia", required = false) MultipartFile[] productMedia,
                                        @RequestPart(value = "skuMedia", required = false) MultipartFile[] skuMedia) {
        ApiResponse<?> response = productService.addProduct(productJson, productMedia, skuMedia);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<?> getAllProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAll());
    }

    @Operation(summary = "Get all products by user")
    @GetMapping("user/{id}")
    public ResponseEntity<?> getAllProductsByUser(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllByUser(id));
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        ApiResponse<?> response = productService.getById(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
