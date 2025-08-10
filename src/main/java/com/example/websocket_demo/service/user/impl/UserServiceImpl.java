package com.example.websocket_demo.service.user.impl;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.service.user.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements IUserService {

    IUserRepository userRepository;
    UserMapper userMapper;
    CloudinaryService mediaUploader;
    PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<?> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userRepository.findAll(Specification.where(UserSpecification.isNotDeleted()), pageable)
                .map(userMapper::toUserDto);
        return users.isEmpty()
                ? new ApiResponse<>(HttpStatus.NO_CONTENT, "No users fetched", users)
                : new ApiResponse<>(HttpStatus.OK, "Users fetched", users);
    }

    @Override
    public ApiResponse<?> createUser(UserModel userModel) {
        if (userModel == null) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "User model cannot be null", null);
        }
        if (userModel.getUsername() == null || userModel.getUsername().isEmpty()) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Username cannot be null or empty", null);
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull(userModel.getUsername()).isPresent()) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Username already exists", null);
        }
        if (userModel.getPassword() == null || userModel.getPassword().isEmpty()) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Password cannot be null or empty", null);
        }
        try {
            userRepository.save(userMapper.toUserEntity(userModel));
            return new ApiResponse<>(HttpStatus.CREATED, "User created successfully", null);
        } catch (IOException | IllegalArgumentException e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }   catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> updateUser(Long id, UserModel userModel) {
        UserEntity currentUser = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        currentUser.setUsername(userModel.getUsername() != null
                ? userModel.getUsername()
                : currentUser.getUsername());
        currentUser.setPassword(userModel.getPassword() != null
                ? passwordEncoder.encode(userModel.getPassword())
                : currentUser.getPassword());
        try {
            currentUser.setProfilePicture(userModel.getProfilePicture() != null
                    ? currentUser.getProfilePicture() != null
                    ? mediaUploader.replaceMediaFile(currentUser.getProfilePicture(), userModel.getProfilePicture())
                    : mediaUploader.uploadMediaFile(userModel.getProfilePicture())
                    : currentUser.getProfilePicture());

            userRepository.save(currentUser);
            return new ApiResponse<>(HttpStatus.OK, "User updated successfully", null);
        } catch (IOException | NoSuchElementException e) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> getUserByUsername(String username) {
        UserDto user = userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::toUserDto)
                .orElse(null);
        return getApiResponse(user);
    }

    @Override
    public ApiResponse<?> getUserById(Long id) {
        UserDto user = userRepository.findByUserIdAndDeletedAtIsNull(id)
                .map(userMapper::toUserDto)
                .orElse(null);
        return getApiResponse(user);
    }

    @Override
    public ApiResponse<?> deleteUser(Long id, Integer isHardDelete) {
        boolean check = isHardDelete != null && isHardDelete == 1;
        UserEntity user = check
                ? userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("User not found"))
                : userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found"));
        try {
            if (check) {
                userRepository.delete(user);
            } else {
                user.setDeletedAt(LocalDateTime.now());
                userRepository.save(user);
            }
            return new ApiResponse<>(HttpStatus.OK, "User deleted", null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }

    private ApiResponse<?> getApiResponse(UserDto user) {
        return user == null
                ? new ApiResponse<>(HttpStatus.NOT_FOUND, "User not found", null)
                : new ApiResponse<>(HttpStatus.OK, "User fetched", user);
    }
}
