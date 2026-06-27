package com.example.websocket_demo.service.email;

import com.example.websocket_demo.dto.request.EmailRequest;

public interface EmailService {
    void sendEmail(EmailRequest email);
}


