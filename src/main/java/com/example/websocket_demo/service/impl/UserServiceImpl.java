package com.example.websocket_demo.service.impl;

import java.util.List;

import com.example.websocket_demo.service.IUserActionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.model.UserModel;
import com.example.websocket_demo.service.IUserService;

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
        String message = users.isEmpty() ? "No users fetched" : "Users fetched";
        HttpStatus status = users.isEmpty() ? HttpStatus.NO_CONTENT : HttpStatus.OK;
        return new ApiResponse<>(status, message, users);
    }

    @Override
    public ApiResponse<?> createUser(UserModel userModel) {
        String message;
        HttpStatus status;

        try {
            int result = userService.createUser(userModel);
            message = result == 1 ? "User created successfully" : "Failed to create user";
            status = result == 1 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        } catch (IllegalArgumentException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = "An unexpected error occurred: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> updateUser(Long id, UserModel userModel) {
        String message;
        HttpStatus status;

        try {
            int result = userService.updateUser(id, userModel);
            message = result == 1 ? "User updated successfully" : "Failed to update user";
            status = result == 1 ? HttpStatus.CREATED : HttpStatus.BAD_REQUEST;
        } catch (UsernameNotFoundException e) {
            message = e.getMessage();
            status = HttpStatus.BAD_REQUEST;
        } catch (Exception e) {
            message = "An unexpected error occurred: " + e.getMessage();
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ApiResponse<>(status, message, null);
    }

    @Override
    public ApiResponse<?> getUserByUsername(String username) {
        UserDto user = userService.getUserByUsername(username);
        String message = user == null ? "User not found" : "User found";
        HttpStatus status = user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return new ApiResponse<>(status, message, user);
    }

    @Override
    public ApiResponse<?> getUserById(Long id) {
        UserDto user = userService.getUserById(id);
        String message = user == null ? "User not found" : "User found";
        HttpStatus status = user == null ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return new ApiResponse<>(status, message, user);
    }

}
