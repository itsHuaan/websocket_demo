package com.example.websocket_demo.mapper;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.repository.IRoleRepository;
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
    IRoleRepository roleRepository;

    public UserDto toUserDto(UserEntity userEntity) {
        return UserDto.builder()
                .userId(userEntity.getId())
                .username(userEntity.getUsername())
                .profilePicture(userEntity.getProfilePicture())
                .createdAt(userEntity.getCreatedAt())
                .modifiedAt(userEntity.getModifiedAt())
                .deletedAt(userEntity.getDeletedAt())
                .build();
    }

    public UserEntity toUserEntity(UserModel userModel) throws IOException {
        RoleEntity role = new RoleEntity();
        if (userModel.getRoleId() != null) {
            role = roleRepository.findById(userModel.getRoleId()).orElseThrow(
                    () -> new NoSuchElementException("Role not found")
            );
        } else {
            role.setId(2L);
        }
        return UserEntity.builder()
                .username(userModel.getUsername())
                .password(passwordEncoder.encode(userModel.getPassword()))
                .profilePicture(userModel.getProfilePicture() != null
                        ? mediaUploader.uploadMediaFile(userModel.getProfilePicture())
                        : null)
                .role(role)
                .build();
    }
}
