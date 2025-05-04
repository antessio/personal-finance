package com.personalfinance.user.service;

import com.personalfinance.user.dto.UserDTO;
import com.personalfinance.user.model.User;

import java.util.Optional;

public interface UserService {
    UserDTO registerUser(String email, String password);
    Optional<User> authenticateUser(String email, String password);
    UserDTO getCurrentUser();

}