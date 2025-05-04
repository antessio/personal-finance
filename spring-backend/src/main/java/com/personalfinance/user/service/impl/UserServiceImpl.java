package com.personalfinance.user.service.impl;

import com.personalfinance.user.dto.UserDTO;
import com.personalfinance.user.model.User;
import com.personalfinance.user.persistence.UserRepository;
import com.personalfinance.user.service.UserService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    @Transactional
    public UserDTO registerUser(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setHashedPassword(passwordEncoder.encode(password));
        // temporary
        user.setConfirmedAt(LocalDateTime.now());

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    public Optional<User> authenticateUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getHashedPassword()))
                .filter(User::isConfirmed);
    }
    @Override
    public UserDTO getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return convertToDTO(user);
    }


    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setConfirmed(user.isConfirmed());
        return dto;
    }
} 