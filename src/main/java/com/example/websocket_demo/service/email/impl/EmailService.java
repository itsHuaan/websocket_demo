package com.example.websocket_demo.service.email.impl;

import com.example.websocket_demo.model.EmailModel;
import com.example.websocket_demo.service.email.IEmailService;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class EmailService implements IEmailService {
    JavaMailSender mailSender;

    @Async
    @Override
    public boolean sendEmail(EmailModel email) {
        return false;
    }
}
