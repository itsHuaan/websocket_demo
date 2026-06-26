package com.example.websocket_demo.service.user.impl;

import com.example.websocket_demo.service.media.impl.CloudinaryServiceImpl;
import com.example.websocket_demo.entity.BaseEntity;
import com.example.websocket_demo.entity.RoleEntity;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.enumeration.AccountStatus;
import com.example.websocket_demo.repository.RoleRepository;
import com.example.websocket_demo.repository.UserRepository;
import com.example.websocket_demo.repository.specification.UserSpecification;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.dto.request.AdminUserRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.response.UserResponse;
import com.example.websocket_demo.dto.request.UserRequest;
import com.example.websocket_demo.service.user.UserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements UserService {

    UserRepository userRepository;
    CloudinaryServiceImpl mediaUploader;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    StringRedisTemplate redisTemplate;
    RoleRepository roleRepository;
    SimpMessagingTemplate messagingTemplate;

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

    // Self-service profile update: only the safe-to-change fields (name + picture).
    // Username/email/password are intentionally excluded — the JWT is keyed on username,
    // so changing it would invalidate the caller's own session.
    @Override
    public UserResponse updateProfile(Long id, UserRequest request) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        try {
            if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
                String url = user.getProfilePicture() != null
                        ? mediaUploader.replaceMediaFile(user.getProfilePicture(), request.getProfilePicture())
                        : mediaUploader.uploadMediaFile(request.getProfilePicture());
                user.setProfilePicture(url);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to update profile picture", e);
        }
        return userMapper.toUserDto(userRepository.save(user));
    }

    // Admin edit: name, username, email, role, status, optional password reset.
    @Override
    public UserResponse adminUpdateUser(Long id, AdminUserRequest request) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already exists");
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
                throw new IllegalArgumentException("Email already exists");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            RoleEntity role = roleRepository.findById(request.getRoleId()).orElseThrow(
                    () -> new NoSuchElementException("Role not found")
            );
            user.setRole(role);
        }
        boolean lockedNow = false;
        if (request.getStatus() != null) {
            user.setStatus(request.getStatus());
            if (request.getStatus() != AccountStatus.ACTIVE.getValue()) {
                user.setStatusReason(request.getStatusReason());
                lockedNow = true;
            } else {
                user.setStatusReason(null); // reactivated — drop the old reason
            }
        }
        UserEntity saved = userRepository.save(user);

        // Tell the user in real time that their account was locked, so they're
        // logged out immediately instead of only on their next request/reload.
        if (lockedNow) {
            Map<String, Object> payload = new HashMap<>();
            payload.put("status", saved.getStatus());
            payload.put("reason", saved.getStatusReason());
            messagingTemplate.convertAndSendToUser(String.valueOf(saved.getUserId()), "/queue/account", payload);
        }
        return userMapper.toUserDto(saved);
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


