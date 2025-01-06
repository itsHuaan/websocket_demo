package com.example.websocket_demo.service;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.MediaUploadTestModel;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITestService {
    ApiResponse<?> getMedias();

    ApiResponse<?> uploadMedia(MediaUploadTestModel model);

    ApiResponse<?> updateMedia(Long id, MediaUploadTestModel model);

    ApiResponse<?> deleteMedia(Long id);
}
