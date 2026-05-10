package com.example.websocket_demo.service.test;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.MediaUploadTestRequest;

public interface ITestService {
    ApiResponse<?> getMedias();

    ApiResponse<?> uploadMedia(MediaUploadTestRequest model);

    ApiResponse<?> updateMedia(Long id, MediaUploadTestRequest model);

    ApiResponse<?> deleteMedia(Long id);
    
    ApiResponse<?> keepServiceAlive();
}

