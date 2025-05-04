package com.personalfinance.util.impl;

import com.personalfinance.util.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class JavaMailEmailService implements EmailService {

    private final JavaMailSender mailSender;
    private final String baseUrl;

    public JavaMailEmailService(JavaMailSender mailSender,
                              @Value("${app.base-url}") String baseUrl) {
        this.mailSender = mailSender;
        this.baseUrl = baseUrl;
    }

    @Override
    public void sendConfirmationEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Confirm your email");
        message.setText(String.format(
            "Please confirm your email by clicking the following link: %s/api/users/confirm?token=%s",
            baseUrl, token
        ));
        mailSender.send(message);
    }

    @Override
    public void sendPasswordResetEmail(String email, String token) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Reset your password");
        message.setText(String.format(
            "Please reset your password by clicking the following link: %s/api/users/reset-password?token=%s",
            baseUrl, token
        ));
        mailSender.send(message);
    }
} 