package com.example.websocket_demo.controller;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.util.Const;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Tag(name = "Chat Controller")
@RequestMapping(value = Const.API_PREFIX + "/chat")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MediaController {
    CloudinaryService mediaUploader;

    @PostMapping("/chat")
    public ResponseEntity<?> uploadMedia(@RequestParam("files") MultipartFile[] files) {
        try {
            if (files.length == 0) {
                return ResponseEntity.badRequest().body(List.of("No files provided"));
            }
            List<String> mediaUrls = mediaUploader.uploadMediaFile(files);
            return ResponseEntity.ok(mediaUrls);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(List.of("Failed to upload files: " + e.getMessage()));
        }
    }
}
