package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.SignInResponse;
import com.example.websocket_demo.dto.UserDto;
import com.example.websocket_demo.model.SignInRequest;
import com.example.websocket_demo.model.SignUpRequest;

public interface IAuthActionService {
    SignInResponse signIn(SignInRequest credentials);
    UserDto signUp(SignUpRequest credentials);
}
