package com.example.websocket_demo.configuration.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;


    public String uploadSingleMediaFile(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult;
        String fileType = file.getContentType();
        String publicId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + UUID.randomUUID();
        if (fileType != null && fileType.startsWith("video")) {
            uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", "video", "public_id", publicId));
        } else {
            uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("public_id", publicId));
        }
        return (String) uploadResult.get("url");
    }

    public String replaceSingleMediaFile(String mediaUrl, MultipartFile newFile) throws IOException {
        Map<String, Object> uploadResult;
        String extractedPublicId = extractPublicId(mediaUrl);
        uploadResult = cloudinary.uploader().upload(newFile.getBytes(),
                ObjectUtils.asMap("public_id", extractedPublicId, "overwrite", true));
        return (String) uploadResult.get("url");
    }

    public boolean deleteSingleMediaFile(String mediaUrl) throws IOException {
        String publicId = extractPublicId(mediaUrl);
        String resourceType = mediaUrl.contains("/video/upload/") ? "video" : "image";
        Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", resourceType));
        return "ok".equals(deleteResult.get("result"));
    }

    private String extractPublicId(String mediaUrl) {
        if (mediaUrl == null || !mediaUrl.contains("/upload/")) {
            throw new IllegalArgumentException("Invalid image URL format: " + mediaUrl);
        }
        String[] parts = mediaUrl.split("/upload/");
        if (parts.length < 2) {
            throw new IllegalArgumentException("Failed to extract public ID from image URL: " + mediaUrl);
        }
        String uploadPath = parts[1];
        return uploadPath.substring(uploadPath.indexOf('/') + 1, uploadPath.lastIndexOf('.'));
    }
}

