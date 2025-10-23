package com.example.websocket_demo.mapper;

import java.io.IOException;
import java.util.NoSuchElementException;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.enumeration.AccountStatus;
import com.example.websocket_demo.model.SignUpRequest;
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
                .userId(userEntity.getUserId())
                .email(userEntity.getEmail())
                .username(userEntity.getUsername())
                .profilePicture(userEntity.getProfilePicture())
                .status(String.valueOf(userEntity.getStatus()))
                .build();
    }

    public UserEntity toUserEntity(UserModel userModel) throws IOException {
        RoleEntity role = new RoleEntity();
        if (userModel.getRoleId() != null) {
            role = roleRepository.findById(userModel.getRoleId()).orElseThrow(
                    () -> new NoSuchElementException("Role not found")
            );
        } else {
            role.setRoleId(2L);
        }
        return UserEntity.builder()
                .email(userModel.getEmail())
                .username(userModel.getUsername())
                .password(passwordEncoder.encode(userModel.getPassword()))
                .profilePicture(userModel.getProfilePicture() != null
                        ? mediaUploader.uploadMediaFile(userModel.getProfilePicture())
                        : null)
                .role(role)
                .status(AccountStatus.ACTIVE.getValue())
                .build();
    }

    public UserEntity toUserEntity(SignUpRequest signUpRequest) {
        RoleEntity role = RoleEntity.builder()
                .roleId(2L)
                .build();
        return UserEntity.builder()
                .email(signUpRequest.getEmail())
                .username(signUpRequest.getUsername())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .status(AccountStatus.INACTIVE.getValue())
                .role(role)
                .build();
    }
}
