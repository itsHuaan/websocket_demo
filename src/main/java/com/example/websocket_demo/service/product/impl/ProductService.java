package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.ProductDto;
import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.model.ProductRequest;
import com.example.websocket_demo.service.product.IProductActionService;
import com.example.websocket_demo.service.product.IProductService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductService implements IProductService {
    IProductActionService productActionService;

    @Override
    public ApiResponse<?> addProduct(ProductRequest productRequest) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String message = "Failed to add product";
        try {
            if (productActionService.addProduct(productRequest) == 1) {
                status = HttpStatus.OK;
                message = "Product added successfully";
            }
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
        }
        return new ApiResponse<>(status, message, null);
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
        HttpStatus status = products != null && !products.isEmpty() ? HttpStatus.OK : HttpStatus.NO_CONTENT;
        String message = products != null && !products.isEmpty() ? "Products fetched" : "No product fetched";
        return new ApiResponse<>(status, message, products);
    }

    @Override
    public ApiResponse<?> getById(Long id) {
        ProductDto product;
        HttpStatus status = HttpStatus.NOT_FOUND;
        String message = "Product not found";
        try {
            product = productActionService.getById(id);
            if (product != null) {
                status = HttpStatus.OK;
                message = "Product fetched";
            }
        } catch (NoSuchElementException e) {
            product = null;
        }

        return new ApiResponse<>(status, message, product);
    }
}
