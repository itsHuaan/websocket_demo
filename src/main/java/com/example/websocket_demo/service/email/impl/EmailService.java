package com.example.websocket_demo.service.email.impl;

import com.example.websocket_demo.dto.request.EmailRequest;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.nio.charset.CharsetEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class EmailService implements com.example.websocket_demo.service.email.EmailService {
    JavaMailSender mailSender;

    @Async
    @Override
    public void sendEmail(EmailRequest email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent(), true);
            mailSender.send(message);
            log.info("Email sent successfully to {}", email.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email.getRecipient(), e.getMessage());
        }
    }
}
