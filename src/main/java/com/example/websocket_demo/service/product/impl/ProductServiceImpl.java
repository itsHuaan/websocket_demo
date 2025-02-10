package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.ProductDto;
import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.service.product.IProductActionService;
import com.example.websocket_demo.service.product.IProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductServiceImpl implements IProductService {
    IProductActionService productActionService;

    @Override
    public ApiResponse<?> addProduct(String productJson, MultipartFile[] productMedia, MultipartFile[] skuMedia) {
        try {
            ProductRequest productRequest = new ObjectMapper().readValue(productJson, ProductRequest.class);
            productRequest.setMedia(productMedia);
            return productActionService.addProduct(productRequest) == 1
                    ? new ApiResponse<>(HttpStatus.CREATED, "Product added successfully", null)
                    : new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to add product", null);
        } catch (JsonProcessingException e) {
            log.error("Error parsing JSON: {}", e.getMessage());
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Invalid JSON", null);
        } catch (IllegalArgumentException e) {
            log.error("Error adding product: {}", e.getMessage());
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            log.error("An unexpected error occurred: {}", e.getMessage());
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
        }
    }

    @Override
    public ApiResponse<?> getAll() {
        List<ProductSummaryDto> products = productActionService.getAll();
        return getApiResponse(products);
    }

    @Override
    public ApiResponse<?> getAllByUser(Long userId) {
        List<ProductSummaryDto> products = productActionService.getAllByUser(userId);
        return getApiResponse(products);
    }

    private ApiResponse<?> getApiResponse(List<ProductSummaryDto> products) {
        return products != null
                ? new ApiResponse<>(HttpStatus.OK, "Product fetched", products)
                : new ApiResponse<>(HttpStatus.NO_CONTENT, "No products fetched", null);
    }

    @Override
    public ApiResponse<?> getById(Long id) {
        try {
            ProductDto product = productActionService.getById(id);
            return new ApiResponse<>(HttpStatus.OK, "Product fetched", product);
        } catch (NoSuchElementException e) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, "Product not found", null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
        }
    }
}
