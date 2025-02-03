package com.example.websocket_demo.controller;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.service.product.IProductService;
import com.example.websocket_demo.util.Const;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping(value = Const.API_PREFIX + "/product")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    IProductService productService;

    @Operation(summary = "Add a product")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<?> addProduct(@RequestPart("product") @Valid String productJson,
                                        @RequestPart(value = "media", required = false) MultipartFile[] media) {
        ProductRequest request;
        try {
            request = new ObjectMapper().readValue(productJson, ProductRequest.class);
            request.setMedia(media);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse<>(
                    HttpStatus.BAD_REQUEST,
                    "Invalid Json",
                    null
            ));
        }
        ApiResponse<?> response = productService.addProduct(request);
        return ResponseEntity.status(response.getStatus()).body(response);
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
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
