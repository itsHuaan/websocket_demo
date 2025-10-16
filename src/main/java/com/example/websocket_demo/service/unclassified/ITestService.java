package com.example.websocket_demo.service.unclassified;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.MediaUploadTestModel;

public interface ITestService {
    ApiResponse<?> getMedias();

    ApiResponse<?> uploadMedia(MediaUploadTestModel model);

    ApiResponse<?> updateMedia(Long id, MediaUploadTestModel model);

    ApiResponse<?> deleteMedia(Long id);
    
    ApiResponse<?> keepServiceAlive();
}
