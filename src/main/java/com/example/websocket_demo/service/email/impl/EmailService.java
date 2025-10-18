package com.example.websocket_demo.service.email.impl;

import com.example.websocket_demo.dto.ApiResponse;
import com.example.websocket_demo.model.EmailModel;
import com.example.websocket_demo.service.email.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class EmailService implements IEmailService {
    JavaMailSender mailSender;

    @Async
    @Override
    public ApiResponse<?> sendEmail(EmailModel email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent());
            mailSender.send(message);
            return new ApiResponse<>(HttpStatus.OK, "Email sent successfully");
        } catch (Exception e) {
            return new ApiResponse<>(HttpStatus.BAD_REQUEST, "Failed to send email");
        }
    }

}
