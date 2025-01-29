package com.example.websocket_demo.service.product.impl;

import com.example.websocket_demo.dto.ApiResponse;
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
            if (productActionService.addProduct(productRequest) == 1){
                status = HttpStatus.OK;
                message = "Product added successfully";
            }
        } catch (IllegalArgumentException e){
            message = e.getMessage();
        }
        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> getAll() {
        List<ProductSummaryDto> products = productActionService.getAll();
        HttpStatus status =  products != null && !products.isEmpty() ? HttpStatus.OK : HttpStatus.NO_CONTENT;
        String message = products != null && !products.isEmpty() ? "Products fetched" : "No product fetched";
        return new ApiResponse<>(status, message, products);
    }
}
