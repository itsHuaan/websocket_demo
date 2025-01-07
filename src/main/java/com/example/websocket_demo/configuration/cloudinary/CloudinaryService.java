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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryService {
    Cloudinary cloudinary;

    public String uploadMediaFile(MultipartFile file) throws IOException {
        Map<String, Object> uploadResult;
        String fileType = file.getContentType();
        String publicId = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + "_" + UUID.randomUUID();
        if (fileType != null) {
            uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("resource_type", fileType.split("/")[0], "public_id", publicId));
        } else {
            uploadResult = cloudinary.uploader().upload(file.getBytes(),
                    ObjectUtils.asMap("public_id", publicId));
        }
        return (String) uploadResult.get("url");
    }

    public String replaceMediaFile(String mediaUrl, MultipartFile newFile) throws IOException {
        Map<String, Object> uploadResult;
        String extractedPublicId = extractPublicId(mediaUrl);
        uploadResult = cloudinary.uploader().upload(newFile.getBytes(),
                ObjectUtils.asMap("public_id", extractedPublicId, "overwrite", true));
        return (String) uploadResult.get("url");
    }

    public boolean deleteMediaFile(String mediaUrl) throws IOException {
        String publicId = extractPublicId(mediaUrl);
        Map<String, Object> deleteResult = cloudinary.uploader().destroy(publicId,
                ObjectUtils.asMap("resource_type", getResourceType(mediaUrl)));
        return "ok".equals(deleteResult.get("result"));
    }

    private String extractPublicId(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        Pattern pattern = Pattern.compile("(\\d{8}_\\d{6}_[\\w-]+)");
        Matcher matcher = pattern.matcher(mediaUrl);
        if (matcher.find()) {
            return matcher.group(0);
        } else {
            throw new IllegalArgumentException("Failed to extract public ID from URL: " + mediaUrl);
        }
    }

    private String getResourceType(String mediaUrl) {
        if (mediaUrl == null || mediaUrl.isEmpty()) {
            throw new IllegalArgumentException("URL cannot be null or empty");
        }
        String mediaUrlLower = mediaUrl.toLowerCase();
        if (mediaUrlLower.endsWith(".mp4") || mediaUrlLower.endsWith(".webm") ||
                mediaUrlLower.endsWith(".mov") || mediaUrlLower.endsWith(".avi") ||
                mediaUrlLower.endsWith(".flv")) {
            return "video";
        } else if (mediaUrlLower.endsWith(".mp3") || mediaUrlLower.endsWith(".ogg") ||
                mediaUrlLower.endsWith(".wav")) {
            return "audio";
        } else if (mediaUrlLower.endsWith(".pdf") || mediaUrlLower.endsWith(".zip") ||
                mediaUrlLower.endsWith(".docx") || mediaUrlLower.endsWith(".psd")) {
            return "raw";
        } else {
            return "image";
        }
    }

}

