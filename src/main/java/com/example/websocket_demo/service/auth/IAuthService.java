package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.response.SignInResponse;
import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;

import com.example.websocket_demo.dto.request.ForgotPasswordRequest;
import com.example.websocket_demo.dto.request.ResetPasswordRequest;
import com.example.websocket_demo.dto.request.VerifyOtpRequest;

public interface IAuthService {
    SignInResponse signIn(SignInRequest credentials);

    void signUp(SignUpRequest credentials);

    void verifySignUp(VerifyOtpRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

