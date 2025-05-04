package com.personalfinance.auth.service;

import com.personalfinance.auth.dto.AuthRequest;
import com.personalfinance.auth.dto.AuthResponse;

public interface AuthService {
    AuthResponse authenticate(AuthRequest request);
    void logout(String token);
} 