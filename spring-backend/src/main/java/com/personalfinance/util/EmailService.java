package com.personalfinance.util;

public interface EmailService {
    void sendConfirmationEmail(String email, String token);
    void sendPasswordResetEmail(String email, String token);
} 