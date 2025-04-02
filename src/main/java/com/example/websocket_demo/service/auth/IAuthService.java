package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.model.SignUpRequest;

public interface IAuthService {
    ApiResponse<?> signIn(SignInRequest credentials);

    ApiResponse<?> signUp(SignUpRequest credentials);
}
