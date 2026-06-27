package com.example.websocket_demo.controller;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;
import com.example.websocket_demo.dto.request.ProductRequest;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.ProductResponse;
import com.example.websocket_demo.dto.response.ProductSummaryResponse;
import com.example.websocket_demo.service.product.ProductService;
import com.example.websocket_demo.common.Const;
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

import java.util.List;

@RestController
@Tag(name = "Product Controller")
@RequestMapping(value = Const.API_PREFIX_V1 + "/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {
    MessageService messageService;
    ProductService productService;

    @Operation(summary = "Add a product")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<ApiResponse<Void>> addProduct(@RequestPart("product") @Valid String productJson,
                                        @RequestPart(value = "productMedia", required = false) MultipartFile[] productMedia,
                                        @RequestPart(value = "skuMedia", required = false) MultipartFile[] skuMedia) throws JsonProcessingException {
        ProductRequest productRequest = new ObjectMapper().readValue(productJson, ProductRequest.class);
        productRequest.setMedia(productMedia);
        productService.addProduct(productRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(HttpStatus.CREATED, messageService.getMessage(PRODUCT_ADDED_SUCCESSFULLY.getCode())));
    }

    @Operation(summary = "Get all products")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getAllProducts() {
        List<ProductSummaryResponse> products = productService.getAll();
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PRODUCT_FETCHED.getCode()), products));
    }

    @Operation(summary = "Get all products by user")
    @GetMapping("user/{id}")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getAllProductsByUser(@PathVariable Long id) {
        List<ProductSummaryResponse> products = productService.getAllByUser(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PRODUCT_FETCHED.getCode()), products));
    }

    @Operation(summary = "Get product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
        ProductResponse product = productService.getById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(new ApiResponse<>(HttpStatus.OK, messageService.getMessage(PRODUCT_FETCHED.getCode()), product));
    }
}