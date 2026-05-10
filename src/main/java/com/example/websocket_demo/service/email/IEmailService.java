package com.example.websocket_demo.service.email;

import com.example.websocket_demo.dto.response.ApiResponse;
import com.example.websocket_demo.dto.request.EmailRequest;

public interface IEmailService {
    ApiResponse<?> sendEmail(EmailRequest email);
}


