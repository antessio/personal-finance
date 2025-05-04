package com.personalfinance.user.service;

import com.personalfinance.user.dto.UserDTO;
import com.personalfinance.user.model.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(String email, String password, String firstName, String lastName);
    Optional<User> authenticateUser(String email, String password);
    void confirmUser(String token);
    void requestPasswordReset(String email);
    void resetPassword(String token, String newPassword);
    UserDTO getCurrentUser();
} 