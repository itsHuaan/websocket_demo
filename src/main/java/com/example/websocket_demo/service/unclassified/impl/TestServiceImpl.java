package com.example.websocket_demo.service.unclassified.impl;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.TestDto;
import com.example.websocket_demo.entity.TestEntity;
import com.example.websocket_demo.entity.TestMediaEntity;
import com.example.websocket_demo.model.MediaUploadTestModel;
import com.example.websocket_demo.repository.ITestRepository;
import com.example.websocket_demo.service.unclassified.ITestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestServiceImpl implements ITestService {
    CloudinaryService mediaUploader;
    ITestRepository testRepository;

    private TestDto toTestDto(TestEntity testEntity) {
        return TestDto.builder()
                .testId(testEntity.getId())
                .mediaUrls(testEntity.getTestMedia().stream()
                        .map(TestMediaEntity::getMediaUrl)
                        .toList())
                .build();
    }

    private TestEntity toTestEntity(MediaUploadTestModel testModel) {
        TestEntity testEntity = TestEntity.builder()
                .testMedia(new ArrayList<>())
                .build();
        List<TestMediaEntity> testMediaEntities = testModel.getMedias() != null
                ? Arrays.stream(testModel.getMedias())
                .map(media -> {
                    try {
                        return TestMediaEntity.builder()
                                .testRecord(testEntity)
                                .mediaUrl(mediaUploader.uploadMediaFile(media))
                                .build();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }).toList()
                : null;
        testEntity.setTestMedia(testMediaEntities);
        return testEntity;
    }

    private TestEntity getTestEntity(Long id) {
        return testRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Test with id " + id + " not found")
        );
    }

    @Override
    public ApiResponse<?> getMedias() {
        List<TestDto> testEntities = testRepository.findAll().stream()
                .map(this::toTestDto)
                .toList();
        return new ApiResponse<>(HttpStatus.OK, "Medias fetched successfully", testEntities);
    }

    @Override
    public ApiResponse<?> uploadMedia(MediaUploadTestModel model) {
        String message;
        HttpStatus status;

        try {
            testRepository.save(toTestEntity(model));
            message = "Media uploaded";
            status = HttpStatus.CREATED;
        } catch (RuntimeException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = "An unexpected error occurred: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> updateMedia(Long id, MediaUploadTestModel model) {
        return null;
    }

    @Override
    public ApiResponse<?> deleteMedia(Long id) {
        String message;
        HttpStatus status;

        try {
            List<String> mediaUrls = getTestEntity(id).getTestMedia().stream()
                    .map(TestMediaEntity::getMediaUrl)
                    .toList();
            for (String mediaUrl : mediaUrls){
                if (mediaUploader.deleteMediaFile(mediaUrl))
                {
                    log.info("Media deleted successfully: {}", mediaUrl);
                } else {
                    log.error("Failed to delete media: {}", mediaUrl);
                }
            }
            testRepository.delete(getTestEntity(id));
            message = "Media deleted";
            status = HttpStatus.CREATED;
        } catch (RuntimeException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = "An unexpected error occurred: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> keepServiceAlive() {
        log.info("Keep service alive at {}", LocalDate.now());
        return new ApiResponse<>(
                HttpStatus.OK,
                "Hello, World!",
                testRepository.getAllIds());
    }

}
