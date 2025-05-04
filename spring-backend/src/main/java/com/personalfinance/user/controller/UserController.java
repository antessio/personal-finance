package com.personalfinance.user.controller;

import com.personalfinance.user.dto.UserDTO;
import com.personalfinance.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        return ResponseEntity.ok(userService.getCurrentUser());
    }

    @PutMapping("/me")
    public ResponseEntity<UserDTO> updateCurrentUser(@Valid @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateCurrentUser(userDTO));
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmEmail(@RequestParam String token) {
        userService.confirmEmail(token);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-confirmation")
    public ResponseEntity<Void> resendConfirmationEmail() {
        userService.resendConfirmationEmail();
        return ResponseEntity.ok().build();
    }
} 