package com.personalfinance.email.service.impl;

import com.personalfinance.email.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.url}")
    private String appUrl;

    public EmailServiceImpl(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendConfirmationEmail(String to, String confirmationToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Confirm your email address");

            Context context = new Context();
            context.setVariable("confirmationUrl", appUrl + "/api/users/confirm?token=" + confirmationToken);
            String htmlContent = templateEngine.process("email/confirmation", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send confirmation email", e);
        }
    }

    @Override
    public void sendPasswordResetEmail(String to, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset your password");

            Context context = new Context();
            context.setVariable("resetUrl", appUrl + "/api/users/reset-password?token=" + resetToken);
            String htmlContent = templateEngine.process("email/password-reset", context);

            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
} 