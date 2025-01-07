package com.example.websocket_demo.mapper;

import java.io.IOException;
import java.time.LocalDateTime;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.model.UserModel;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserMapper {
    PasswordEncoder passwordEncoder;
    CloudinaryService mediaUploader;

    public UserDto toUserDto(UserEntity userEntity) {
        return UserDto.builder()
                .userId(userEntity.getUserId())
                .username(userEntity.getUsername())
                .profilePicture(userEntity.getProfilePicture())
                .createdAt(userEntity.getCreatedAt())
                .build();
    }

    public UserEntity toUserEntity(UserModel userModel) throws IOException {
        return UserEntity.builder()
                .username(userModel.getUsername())
                .password(passwordEncoder.encode(userModel.getPassword()))
                .profilePicture(userModel.getProfilePicture() != null
                        ? mediaUploader.uploadMediaFile(userModel.getProfilePicture())
                        : null)
                .role(userModel.getRoleId() != null
                        ? RoleEntity.builder().roleId(userModel.getRoleId()).build()
                        : RoleEntity.builder().roleId(2L).build())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
