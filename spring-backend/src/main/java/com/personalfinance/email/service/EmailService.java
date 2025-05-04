package com.personalfinance.email.service;

public interface EmailService {
    void sendConfirmationEmail(String to, String confirmationToken);
    void sendPasswordResetEmail(String to, String resetToken);
} 