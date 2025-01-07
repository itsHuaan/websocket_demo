package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.SignInResponse;
import com.example.websocket_demo.model.SignInRequest;

public interface IAuthActionService {
    SignInResponse signIn(SignInRequest credentials);
}
