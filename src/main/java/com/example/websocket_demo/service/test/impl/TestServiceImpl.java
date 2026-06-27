package com.example.websocket_demo.service.test.impl;

import com.example.websocket_demo.common.DataUtil;
import com.example.websocket_demo.service.test.TestService;
import com.example.websocket_demo.service.media.impl.CloudinaryServiceImpl;
import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.TestResponse;
import com.example.websocket_demo.entity.TestEntity;
import com.example.websocket_demo.entity.TestMediaEntity;
import com.example.websocket_demo.dto.request.MediaUploadTestRequest;
import com.example.websocket_demo.repository.TestRepository;
import com.example.websocket_demo.common.DateUtil;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.websocket_demo.common.MessageService;
import static com.example.websocket_demo.enumeration.ResponseMessage.*;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestServiceImpl implements TestService {
    CloudinaryServiceImpl mediaUploader;
    TestRepository testRepository;
    MessageService messageService;

    private TestResponse toTestDto(TestEntity testEntity) {
        return TestResponse.builder()
                .testId(testEntity.getId())
                .mediaUrls(testEntity.getTestMedia().stream()
                        .map(TestMediaEntity::getMediaUrl)
                        .toList())
                .build();
    }

    private TestEntity toTestEntity(MediaUploadTestRequest testModel) {
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
                () -> new IllegalArgumentException(messageService.getMessage(TEST_NOT_FOUND.getCode()) + id)
        );
    }

    @Override
    public ApiResponse<?> getMedias() {
        List<TestResponse> testEntities = testRepository.findAll().stream()
                .map(this::toTestDto)
                .toList();
        return new ApiResponse<>(HttpStatus.OK, messageService.getMessage(MEDIAS_FETCHED.getCode()), testEntities);
    }

    @Override
    public ApiResponse<?> uploadMedia(MediaUploadTestRequest model) {
        String message;
        HttpStatus status;

        try {
            testRepository.save(toTestEntity(model));
            message = messageService.getMessage(MEDIA_UPLOADED.getCode());
            status = HttpStatus.CREATED;
        } catch (RuntimeException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = messageService.getMessage(UNEXPECTED_ERROR.getCode()) + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ApiResponse<>(status, message);
    }

    @Override
    public ApiResponse<?> updateMedia(Long id, MediaUploadTestRequest model) {
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
            message = messageService.getMessage(MEDIA_DELETED.getCode());
            status = HttpStatus.CREATED;
        } catch (RuntimeException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = messageService.getMessage(UNEXPECTED_ERROR.getCode()) + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ApiResponse<>(status, message);
    }

    @Override
    public ApiResponse<?> keepServiceAlive() {
        String time = DateUtil.formatDate(LocalDateTime.now(), "HH:mm:ss MMM dd, yyyy");
        log.info("Service is alive: {}", time);
        return new ApiResponse<>(
                HttpStatus.OK,
                time,
                DataUtil.isNullOrZero(testRepository.getAllIds()) ? 0 : testRepository.getAllIds().size(),
                LocalDateTime.of(2001, 9, 19, 0, 0));
    }

}

