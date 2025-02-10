package com.example.websocket_demo.service.auth.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.dto.SignInResponse;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.service.auth.IAuthActionService;
import com.example.websocket_demo.service.auth.IAuthService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class AuthServiceImpl implements IAuthService {
    IAuthActionService authActionService;

    @Override
    public ApiResponse<?> signIn(SignInRequest credentials) {
        try {
            SignInResponse signInResponse = authActionService.signIn(credentials);
            return new ApiResponse<>(HttpStatus.OK, "You're now logged in", signInResponse);
        } catch (RuntimeException e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, e.getMessage(), null);
        }
    }
}
