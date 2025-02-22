package com.example.websocket_demo.service.user.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import com.example.websocket_demo.configuration.cloudinary.CloudinaryService;
import com.example.websocket_demo.entity.UserEntity;
import com.example.websocket_demo.specification.UserSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.mapper.UserMapper;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.repository.IUserRepository;
import com.example.websocket_demo.service.user.IUserActionService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActionServiceImpl implements IUserActionService {

    IUserRepository userRepository;
    UserMapper userMapper;
    CloudinaryService mediaUploader;
    PasswordEncoder passwordEncoder;

    @Override
    public Page<UserDto> getAllUsers(Pageable pageable) {
        return userRepository.findAll(Specification.where(UserSpecification.isNotDeleted()), pageable)
                .map(userMapper::toUserDto);
    }

    @Override
    public int createUser(UserModel userModel) {
        if (userModel == null) {
            throw new IllegalArgumentException("User model cannot be null");
        }
        if (userModel.getUsername() == null || userModel.getUsername().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty");
        }
        if (userRepository.findByUsernameAndDeletedAtIsNull(userModel.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userModel.getPassword() == null || userModel.getPassword().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        try {
            userRepository.save(userMapper.toUserEntity(userModel));
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NoSuchElementException e) {
            throw new RuntimeException(e.getMessage());
        }
        return 1;
    }

    @Override
    public int updateUser(Long id, UserModel userModel) {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        userRepository.save(currentUser);
        return 1;
    }

    @Override
    public UserDto getUserByUsername(String username) {
        return userRepository.findByUsernameAndDeletedAtIsNull(username)
                .map(userMapper::toUserDto)
                .orElse(null);
    }

    @Override
    public UserDto getUserById(Long id) {
        return userRepository.findByUserIdAndDeletedAtIsNull(id)
                .map(userMapper::toUserDto)
                .orElse(null);
    }

    @Override
    public int deleteUser(Long id) {
        UserEntity user = userRepository.findByUserIdAndDeletedAtIsNull(id).orElseThrow(
                () -> new NoSuchElementException("User not found")
        );
        try {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return 1;
        } catch (Exception e) {
            return 0;
        }
    }
}
