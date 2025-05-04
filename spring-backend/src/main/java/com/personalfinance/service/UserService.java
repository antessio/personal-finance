package com.personalfinance.service;

import com.personalfinance.dto.UserDTO;
import com.personalfinance.model.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(String email, String password, String firstName, String lastName);
    Optional<User> authenticateUser(String email, String password);
    void confirmUser(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    UserDTO getCurrentUser();
} 