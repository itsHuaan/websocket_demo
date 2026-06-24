package com.example.websocket_demo.service.product;

import com.example.websocket_demo.dto.request.ProductRequest;
import com.example.websocket_demo.dto.response.ProductResponse;
import com.example.websocket_demo.dto.response.ProductSummaryResponse;

import java.util.List;

public interface ProductService {
    void addProduct(ProductRequest productRequest);
    List<ProductSummaryResponse> getAll();
    List<ProductSummaryResponse> getAllByUser(Long userId);
    ProductResponse getById(Long id);
}
