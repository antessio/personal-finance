package com.personalfinance.auth.service.impl;

import com.personalfinance.auth.dto.AuthRequest;
import com.personalfinance.auth.dto.AuthResponse;
import com.personalfinance.auth.service.AuthService;
import com.personalfinance.security.JwtTokenUtil;
import com.personalfinance.user.model.User;
import com.personalfinance.user.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserService userService;

    public AuthServiceImpl(
            AuthenticationManager authenticationManager,
            UserDetailsService userDetailsService,
            JwtTokenUtil jwtTokenUtil,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.jwtTokenUtil = jwtTokenUtil;
        this.userService = userService;
    }

    @Override
    public AuthResponse authenticate(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtTokenUtil.generateToken(userDetails);

        User user = userService.authenticateUser(request.getEmail(), request.getPassword())
            .orElseThrow(() -> new RuntimeException("User not found"));

        AuthResponse response = new AuthResponse();
        response.setToken(token);
        response.setEmail(user.getEmail());

        response.setConfirmed(user.getConfirmedAt() != null);

        return response;
    }

    @Override
    public void logout(String token) {
        // In a stateless JWT system, we don't need to do anything on the server side
        // The client should remove the token from their storage
        SecurityContextHolder.clearContext();
    }
} 