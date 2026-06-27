package com.example.websocket_demo.service.user.impl;

import com.example.websocket_demo.common.MessageService;
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
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
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

import static com.example.websocket_demo.enumeration.ResponseMessage.*;

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
    MessageService messageService;

    @NonFinal
    @Value("${app.feature.change-username.enabled:false}")
    boolean isUsernameChangeEnabled;

    @NonFinal
    @Value("${app.feature.change-username.cooldown-days:30}")
    int usernameChangeCooldownDays;

    @Override
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(Specification.where(UserSpecification.isNull(BaseEntity.Fields.deletedAt)), pageable)
                .map(userMapper::toUserDto);
    }

    @Override
    public UserResponse createUser(UserRequest UserRequest) {
        if (UserRequest == null) {
            throw new IllegalArgumentException(messageService.getMessage(USER_INFO_NULL.getCode()));
        }
        if (UserRequest.getFirstName() == null || UserRequest.getFirstName().trim().isEmpty() ||
            UserRequest.getLastName() == null || UserRequest.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException(messageService.getMessage(PLEASE_ENTER_FIRST_AND_LAST_NAME.getCode()));
        }
        if (UserRequest.getEmail() == null || !UserRequest.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException(messageService.getMessage(PLEASE_ENTER_A_VALID_EMAIL_ADDRESS.getCode()));
        }
        if (UserRequest.getUsername() == null || UserRequest.getUsername().trim().length() < 3) {
            throw new IllegalArgumentException(messageService.getMessage(USERNAME_MUST_BE_AT_LEAST_3_CHARACTERS.getCode()));
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull(UserRequest.getUsername()).isPresent()) {
            throw new IllegalArgumentException(messageService.getMessage(USERNAME_EXISTS.getCode()));
        }
        if (UserRequest.getPassword() == null || UserRequest.getPassword().length() < 6) {
            throw new IllegalArgumentException(messageService.getMessage(PASSWORD_MUST_BE_AT_LEAST_6_CHARACTERS.getCode()));
        }
        try {
            UserEntity user = userMapper.toUserEntity(UserRequest);
            return userMapper.toUserDto(userRepository.save(user));
        } catch (IOException e) {
            throw new RuntimeException(messageService.getMessage(FAILED_TO_UPLOAD_MEDIA.getCode()), e);
        }
    }

    @Override
    public void updateUser(Long id, UserRequest UserRequest) {
        UserEntity currentUser = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode()))
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
            throw new RuntimeException(messageService.getMessage(PROFILE_PICTURE_UPDATE_FAILED.getCode()), e);
        }
    }

    // Self-service profile update: only the safe-to-change fields (name + picture).
    // Username can be changed if the feature flag is enabled and cooldown is met.
    @Override
    public UserResponse updateProfile(Long id, UserRequest request) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode()))
        );
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (isUsernameChangeEnabled && request.getUsername() != null && !request.getUsername().isBlank()) {
            String newUsername = request.getUsername();
            if (!newUsername.equals(user.getUsername())) {
                if (newUsername.equals(user.getPreviousUsername())) {
                    throw new IllegalArgumentException(messageService.getMessage(CANNOT_USE_PREVIOUS_USERNAME.getCode()));
                }
                if (user.getLastUsernameChangeDate() != null) {
                    LocalDateTime nextAllowedChange = user.getLastUsernameChangeDate().plusDays(usernameChangeCooldownDays);
                    if (LocalDateTime.now().isBefore(nextAllowedChange)) {
                        throw new IllegalArgumentException(messageService.getMessage(USERNAME_CHANGE_COOLDOWN.getCode(), usernameChangeCooldownDays));
                    }
                }
                if (userRepository.existsByUsernameAndDeletedAtIsNull(newUsername)) {
                    throw new IllegalArgumentException(messageService.getMessage(USERNAME_EXISTS.getCode()));
                }
                user.setPreviousUsername(user.getUsername());
                user.setUsername(newUsername);
                user.setLastUsernameChangeDate(LocalDateTime.now());
            }
        }
        try {
            if (request.getProfilePicture() != null && !request.getProfilePicture().isEmpty()) {
                String url = user.getProfilePicture() != null
                        ? mediaUploader.replaceMediaFile(user.getProfilePicture(), request.getProfilePicture())
                        : mediaUploader.uploadMediaFile(request.getProfilePicture());
                user.setProfilePicture(url);
            }
        } catch (IOException e) {
            throw new RuntimeException(messageService.getMessage(PROFILE_PICTURE_UPDATE_FAILED.getCode()), e);
        }
        return userMapper.toUserDto(userRepository.save(user));
    }

    // Admin edit: name, username, email, role, status, optional password reset.
    @Override
    public UserResponse adminUpdateUser(Long id, AdminUserRequest request) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode()))
        );

        if (request.getEmail() != null && !request.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException(messageService.getMessage(PLEASE_ENTER_A_VALID_EMAIL_ADDRESS.getCode()));
        }
        if (request.getUsername() != null && request.getUsername().length() < 3) {
            throw new IllegalArgumentException(messageService.getMessage(USERNAME_MUST_BE_AT_LEAST_3_CHARACTERS.getCode()));
        }
        if (request.getPassword() != null && !request.getPassword().isEmpty() && request.getPassword().length() < 6) {
            throw new IllegalArgumentException(messageService.getMessage(PASSWORD_MUST_BE_AT_LEAST_6_CHARACTERS.getCode()));
        }

        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()
                && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsernameAndDeletedAtIsNull(request.getUsername()).isPresent()) {
                throw new IllegalArgumentException(messageService.getMessage(USERNAME_EXISTS.getCode()));
            }
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null && !request.getEmail().isBlank()
                && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
                throw new IllegalArgumentException(messageService.getMessage(EMAIL_EXISTS.getCode()));
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getRoleId() != null) {
            RoleEntity role = roleRepository.findById(request.getRoleId()).orElseThrow(
                    () -> new NoSuchElementException(messageService.getMessage(ROLE_NOT_FOUND.getCode()))
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

        // Tell the user in real time that their account was modified.
        // If locked, we send the specific reason. Otherwise, we send an UPDATED action to log them out.
        Map<String, Object> payload = new HashMap<>();
        if (lockedNow) {
            payload.put("status", saved.getStatus());
            payload.put("reason", saved.getStatusReason());
        } else {
            payload.put("action", "UPDATED");
        }
        messagingTemplate.convertAndSendToUser(String.valueOf(saved.getUserId()), "/queue/account", payload);

        return userMapper.toUserDto(saved);
    }

    @Override
    public UserResponse getUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode())));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return userRepository.findByUserIdAndDeletedAtIsNull(id)
                .map(userMapper::toUserDto)
                .orElseThrow(() -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode())));
    }

    @Override
    public void deleteUser(Long id, Integer isHardDelete) {
        boolean check = isHardDelete != null && isHardDelete == 1;
        UserEntity user = check
                ? userRepository.findById(id).orElseThrow(
                () -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode())))
                : userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException(messageService.getMessage(USER_NOT_FOUND.getCode())));
        if (check) {
            userRepository.delete(user);
        } else {
            String suffix = "_del_" + user.getUserId();
            user.setDeletedAt(LocalDateTime.now());
            user.setEmail(user.getEmail() + suffix);
            
            String newUsername = user.getUsername() + suffix;
            if (newUsername.length() > 50) {
                newUsername = user.getUsername().substring(0, 50 - suffix.length()) + suffix;
            }
            user.setUsername(newUsername);
            
            if (user.getPhoneNumber() != null) {
                user.setPhoneNumber(user.getPhoneNumber() + suffix);
            }
            userRepository.save(user);
        }

        // Immediately log out the user if they are online
        Map<String, Object> payload = new HashMap<>();
        payload.put("action", "DELETED");
        messagingTemplate.convertAndSendToUser(String.valueOf(id), "/queue/account", payload);
    }

    @Override
    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members("chat:online_users");
    }
}


