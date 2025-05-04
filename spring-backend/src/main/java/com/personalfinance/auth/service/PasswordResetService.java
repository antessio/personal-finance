package com.personalfinance.auth.service;

public interface PasswordResetService {
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    boolean isValidToken(String token);
} 