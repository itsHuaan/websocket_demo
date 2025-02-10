package com.example.websocket_demo.service.user.impl;

import com.example.websocket_demo.service.user.IUserActionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.service.user.IUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserServiceImpl implements IUserService {

    IUserActionService userService;

    @Override
    public ApiResponse<?> getAllUsers(Pageable pageable) {
        Page<UserDto> users = userService.getAllUsers(pageable);
        return users.isEmpty()
                ? new ApiResponse<>(HttpStatus.NO_CONTENT, "No users fetched", users)
                : new ApiResponse<>(HttpStatus.OK, "Users fetched", users);
    }

    @Override
    public ApiResponse<?> createUser(UserModel userModel) {
        try {
            return userService.createUser(userModel) == 1
                    ? new ApiResponse<>(HttpStatus.CREATED, "User created successfully", null)
                    : new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to create user", null);
        } catch (IllegalArgumentException e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> updateUser(Long id, UserModel userModel) {
        try {
            return userService.updateUser(id, userModel) == 1
                    ? new ApiResponse<>(HttpStatus.OK, "User updated successfully", null)
                    : new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to update user", null);
        } catch (UsernameNotFoundException e) {
            return new ApiResponse<>(HttpStatus.NOT_FOUND, e.getMessage(), null);
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage(), null);
        }
    }

    @Override
    public ApiResponse<?> getUserByUsername(String username) {
        UserDto user = userService.getUserByUsername(username);
        return getApiResponse(user);
    }

    @Override
    public ApiResponse<?> getUserById(Long id) {
        UserDto user = userService.getUserById(id);
        return getApiResponse(user);
    }

    private ApiResponse<?> getApiResponse(UserDto user) {
        return user == null
                ? new ApiResponse<>(HttpStatus.NOT_FOUND, "User not found", null)
                : new ApiResponse<>(HttpStatus.OK, "User fetched", user);
    }
}
