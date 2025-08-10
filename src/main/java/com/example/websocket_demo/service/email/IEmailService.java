package com.example.websocket_demo.service.email;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.EmailModel;

public interface IEmailService {
    ApiResponse<?> sendEmail(EmailModel email);
}
