package com.personalfinance.service.impl;

import com.personalfinance.dto.UserDTO;
import com.personalfinance.model.User;
import com.personalfinance.model.UserToken;
import com.personalfinance.repository.UserRepository;
import com.personalfinance.repository.UserTokenRepository;
import com.personalfinance.service.UserService;
import com.personalfinance.util.EmailService;
import com.personalfinance.util.PasswordEncoder;
import com.personalfinance.util.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserTokenRepository userTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;
    private final EmailService emailService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                          UserTokenRepository userTokenRepository,
                          PasswordEncoder passwordEncoder,
                          TokenGenerator tokenGenerator,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.userTokenRepository = userTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public UserDTO registerUser(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already taken");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setConfirmed(false);

        User savedUser = userRepository.save(user);

        // Generate confirmation token
        String token = tokenGenerator.generateToken();
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setContext("confirm");
        userToken.setUser(savedUser);
        userTokenRepository.save(userToken);

        // Send confirmation email
        emailService.sendConfirmationEmail(email, token);

        return convertToDTO(savedUser);
    }

    @Override
    public Optional<User> authenticateUser(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPassword()))
                .filter(User::isConfirmed);
    }

    @Override
    @Transactional
    public void confirmUser(String token) {
        UserToken userToken = userTokenRepository.findByTokenAndContext(token, "confirm")
                .orElseThrow(() -> new IllegalArgumentException("Invalid confirmation token"));

        User user = userToken.getUser();
        user.setConfirmed(true);
        user.setConfirmedAt(LocalDateTime.now());
        userRepository.save(user);

        userTokenRepository.delete(userToken);
    }

    @Override
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Generate reset token
        String token = tokenGenerator.generateToken();
        UserToken userToken = new UserToken();
        userToken.setToken(token);
        userToken.setContext("reset_password");
        userToken.setUser(user);
        userTokenRepository.save(userToken);

        // Send reset email
        emailService.sendPasswordResetEmail(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        UserToken userToken = userTokenRepository.findByTokenAndContext(token, "reset_password")
                .orElseThrow(() -> new IllegalArgumentException("Invalid reset token"));

        User user = userToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        userTokenRepository.delete(userToken);
    }

    @Override
    public UserDTO getCurrentUser() {
        // This will be implemented with Spring Security context
        throw new UnsupportedOperationException("Not implemented yet");
    }

    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setConfirmed(user.isConfirmed());
        return dto;
    }
} 