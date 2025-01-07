package com.example.websocket_demo.controller;

import com.example.websocket_demo.model.MediaUploadTestModel;
import com.example.websocket_demo.service.unclassified.ITestService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.util.Const;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@Tag(name = "Test Controller")
@RequestMapping(value = Const.API_PREFIX + "/test")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TestController {

    ITestService testService;

    @Operation(summary = "This is my first test controller")
    @GetMapping("/first")
    public ResponseEntity<?> firstTestMethod(@RequestParam String url) {
        Pattern pattern = Pattern.compile("(\\d{8}_\\d{6}_[\\w-]+)");
        Matcher matcher = pattern.matcher(url);
        String publicId = matcher.find() ? matcher.group(0) : null;
        return ResponseEntity.status(HttpStatus.OK).body(
                new ApiResponse<>(
                        HttpStatus.OK,
                        "This controller for testing",
                        publicId));
    }

    @GetMapping("/get-upload")
    public ResponseEntity<?> getMedia() {
        ApiResponse<?> response = testService.getMedias();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadMedia(MediaUploadTestModel model) {
        ApiResponse<?> response = testService.uploadMedia(model);
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteMedia(@PathVariable Long id) {
        ApiResponse<?> response = testService.deleteMedia(id);
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}
