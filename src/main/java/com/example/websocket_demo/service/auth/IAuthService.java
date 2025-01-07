package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.SignInRequest;

public interface IAuthService {
    ApiResponse<?> signIn(SignInRequest credentials);
}
