package com.personalfinance.auth.service.impl;

import com.personalfinance.auth.model.UserToken;
import com.personalfinance.auth.repository.UserTokenRepository;
import com.personalfinance.auth.service.PasswordResetService;
import com.personalfinance.email.service.EmailService;
import com.personalfinance.user.model.User;
import com.personalfinance.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private static final DateTimeFormatter SQL_TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Value("${app.password-reset.token-expiration-hours}")
    private int tokenExpirationHours;

    public PasswordResetServiceImpl(
            UserRepository userRepository,
            UserTokenRepository userTokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    String token = UUID.randomUUID().toString();
                    LocalDateTime expiresAt = LocalDateTime.now().plusHours(tokenExpirationHours);
                    
                    userTokenRepository.createToken(
                            user.getId(),
                            token,
                            UserToken.TokenType.PASSWORD_RESET.name(),
                            expiresAt.format(SQL_TIMESTAMP));
                    emailService.sendPasswordResetEmail(user.getEmail(), token);
                });
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        userTokenRepository.findByTokenAndTokenType(token, UserToken.TokenType.PASSWORD_RESET)
                .filter(userToken -> userToken.getExpiresAt().isAfter(LocalDateTime.now()))
                .ifPresent(userToken -> {
                    User user = userRepository.findById(userToken.getUserId())
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    
                    user.setHashedPassword(passwordEncoder.encode(newPassword));
                    userRepository.save(user);
                    userTokenRepository.delete(userToken);
                });
    }

    @Override
    public boolean isValidToken(String token) {
        return userTokenRepository.findByTokenAndTokenType(token, UserToken.TokenType.PASSWORD_RESET)
                .filter(userToken -> userToken.getExpiresAt().isAfter(LocalDateTime.now()))
                .isPresent();
    }
} 