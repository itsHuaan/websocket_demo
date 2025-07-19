package com.example.websocket_demo.service.email;

import com.example.websocket_demo.model.EmailModel;

public interface IEmailService {
    boolean sendEmail(EmailModel email);
}
