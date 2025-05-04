package com.personalfinance.auth.controller;

import com.personalfinance.auth.dto.PasswordResetRequest;
import com.personalfinance.auth.dto.PasswordResetTokenRequest;
import com.personalfinance.auth.service.PasswordResetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        passwordResetService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetTokenRequest request) {
        if (!passwordResetService.isValidToken(request.getToken())) {
            return ResponseEntity.badRequest().body("Invalid or expired token");
        }

        passwordResetService.resetPassword(request.getToken(), request.getPassword());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        boolean isValid = passwordResetService.isValidToken(token);
        return ResponseEntity.ok(isValid);
    }
} 