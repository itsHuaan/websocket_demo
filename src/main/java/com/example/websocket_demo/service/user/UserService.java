package com.example.websocket_demo.service.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.websocket_demo.dto.response.UserResponse;
import com.example.websocket_demo.dto.request.UserRequest;
import com.example.websocket_demo.dto.request.AdminUserRequest;

public interface UserService {
    Page<UserResponse> getAllUsers(Pageable pageable);

    UserResponse createUser(UserRequest UserRequest);

    void updateUser(Long id, UserRequest UserRequest);

    UserResponse updateProfile(Long id, UserRequest request);

    UserResponse adminUpdateUser(Long id, AdminUserRequest request);

    UserResponse getUserByUsername(String username);

    UserResponse getUserById(Long id);

    void deleteUser(Long id, Integer isHardDelete);

    java.util.Set<String> getOnlineUsers();

    java.util.List<UserResponse> getConnectedUsers(Long userId);
}


