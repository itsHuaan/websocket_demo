package com.example.websocket_demo.service.user.impl;

import com.example.websocket_demo.service.media.CloudinaryService;
import com.example.websocket_demo.entity.BaseEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.repository.specification.UserSpecification;
import com.example.websocket_demo.mapper.UserMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.response.UserResponse;
import com.example.websocket_demo.dto.request.UserRequest;
import com.example.websocket_demo.service.user.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.data.redis.core.StringRedisTemplate;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements IUserService {

    IUserRepository userRepository;
    CloudinaryService mediaUploader;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    StringRedisTemplate redisTemplate;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(Specification.where(UserSpecification.isNull(BaseEntity.Fields.deletedAt)), pageable)
                .map(userMapper::toUserDto);
    }

    @Override
    public UserResponse createUser(UserRequest UserRequest) {
        if (UserRequest == null) {
            throw new IllegalArgumentException("User model cannot be null");
        }
        if (UserRequest.getUsername() == null || UserRequest.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull(UserRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (UserRequest.getPassword() == null || UserRequest.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        try {
            UserEntity user = userMapper.toUserEntity(UserRequest);
            return userMapper.toUserDto(userRepository.save(user));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload profile picture", e);
        }
    }

    @Override
    public void updateUser(Long id, UserRequest UserRequest) {
        UserEntity currentUser = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        currentUser.setUsername(UserRequest.getUsername() != null
                ? UserRequest.getUsername()
                : currentUser.getUsername());
        currentUser.setFirstName(UserRequest.getFirstName() != null
                ? UserRequest.getFirstName()
                : currentUser.getFirstName());
        currentUser.setLastName(UserRequest.getLastName() != null
                ? UserRequest.getLastName()
                : currentUser.getLastName());
        currentUser.setPassword(UserRequest.getPassword() != null
                ? passwordEncoder.encode(UserRequest.getPassword())
                : currentUser.getPassword());
        try {
            currentUser.setProfilePicture(UserRequest.getProfilePicture() != null
                    ? currentUser.getProfilePicture() != null
                    ? mediaUploader.replaceMediaFile(currentUser.getProfilePicture(), UserRequest.getProfilePicture())
                    : mediaUploader.uploadMediaFile(UserRequest.getProfilePicture())
                    : currentUser.getProfilePicture());

            userRepository.save(currentUser);
        } catch (IOException e) {
            throw new RuntimeException("Failed to update profile picture", e);
        }
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findByUserIdAndDeletedAtIsNull(id)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException("User not found"));
    }

    @Override
    public void deleteUser(Long id, Integer isHardDelete) {
        boolean check = isHardDelete != null && isHardDelete == 1;
        UserEntity user = check
                ? userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException("User not found"))
                : userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found"));
        if (check) {
            userRepository.delete(user);
        } else {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
        }
    }

    @Override
    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members("chat:online_users");
    }
}


