package com.example.websocket_demo.configuration.cloudinary;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinaryConfig {
    @Value("${cloudinary.name}")
    String NAME;

    @Value("${cloudinary.api-key}")
    String API_KEY;

    @Value("${cloudinary.secret-key}")
    String SECRET_KEY;

    @Bean
    Cloudinary cloudinary() {
        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", NAME,
                "api_key", API_KEY,
                "api_secret", SECRET_KEY));
    }
}
