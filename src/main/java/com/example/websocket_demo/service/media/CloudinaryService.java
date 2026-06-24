package com.example.websocket_demo.service.media;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface CloudinaryService {
    List<String> uploadMediaFile(MultipartFile[] file) throws IOException;
    String uploadMediaFile(MultipartFile file) throws IOException;
    String replaceMediaFile(String mediaUrl, MultipartFile newFile) throws IOException;
    boolean deleteMediaFile(String mediaUrl) throws IOException;
    List<String> getAllMedia() throws Exception;
}
