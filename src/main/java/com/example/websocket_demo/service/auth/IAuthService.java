package com.example.websocket_demo.service.auth;

import com.example.websocket_demo.dto.response.SignInResponse;
import com.example.websocket_demo.dto.response.PhoneCodeResponse;
import com.example.websocket_demo.dto.request.SignInRequest;
import com.example.websocket_demo.dto.request.SignUpRequest;

import java.util.List;

import com.example.websocket_demo.dto.request.ForgotPasswordRequest;
import com.example.websocket_demo.dto.request.ResetPasswordRequest;
import com.example.websocket_demo.dto.request.VerifyOtpRequest;
import com.example.websocket_demo.dto.request.ResendOtpRequest;

public interface IAuthService {
    SignInResponse signIn(SignInRequest credentials);

    SignInResponse refreshToken(String refreshToken);

    void logout(String refreshToken);

    void signUp(SignUpRequest credentials);

    void verifySignUp(VerifyOtpRequest request);

    void resendSignUpOtp(ResendOtpRequest request);

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}

