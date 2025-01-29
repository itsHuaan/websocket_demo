package com.example.websocket_demo.service.product;

import com.example.websocket_demo.dto.ProductDto;
import com.example.websocket_demo.dto.ProductSummaryDto;
import com.example.websocket_demo.model.ProductRequest;

import java.util.List;

public interface IProductActionService {
    int addProduct(ProductRequest productRequest);
    List<ProductSummaryDto> getAll();
    List<ProductSummaryDto> getAllByUser(Long userId);
    ProductDto getById(Long id);
}
