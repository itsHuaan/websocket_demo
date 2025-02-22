package com.example.websocket_demo.service.user;

import org.springframework.data.domain.Pageable;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.UserModel;

public interface IUserService {
    ApiResponse<?> getAllUsers(Pageable pageable);

    ApiResponse<?> createUser(UserModel userModel);

    ApiResponse<?> updateUser(Long id, UserModel userModel);

    ApiResponse<?> getUserByUsername(String username);

    ApiResponse<?> getUserById(Long id);

    ApiResponse<?> deleteUser(Long id);
}
