package com.example.websocket_demo.service.email.impl;

import com.example.websocket_demo.dto.request.EmailRequest;
import com.example.websocket_demo.service.email.IEmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true)
public class EmailService implements IEmailService {
    JavaMailSender mailSender;

    @Async
    @Override
    public void sendEmail(EmailRequest email) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(email.getRecipient());
            helper.setSubject(email.getSubject());
            helper.setText(email.getContent());
            mailSender.send(message);
            log.info("Email sent successfully to {}", email.getRecipient());
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", email.getRecipient(), e.getMessage());
        }
    }
}
