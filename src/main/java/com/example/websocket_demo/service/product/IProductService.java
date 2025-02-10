package com.example.websocket_demo.service.product;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.ProductRequest;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.springframework.web.multipart.MultipartFile;

public interface IProductService {
    ApiResponse<?> addProduct(String productJson, MultipartFile[] productMedia, MultipartFile[] skuMedia);
    ApiResponse<?> getAll();
    ApiResponse<?> getAllByUser(Long userId);
    ApiResponse<?> getById(Long id);
}
