package com.personalfinance.user.api;

import com.personalfinance.user.dto.UserDTO;
import com.personalfinance.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody Map<String, String> request) {
        UserDTO user = userService.registerUser(
            request.get("email"),
            request.get("password"),
            request.get("firstName"),
            request.get("lastName")
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirm(@RequestBody Map<String, String> request) {
        userService.confirmUser(request.get("token"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody Map<String, String> request) {
        userService.requestPasswordReset(request.get("email"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody Map<String, String> request) {
        userService.resetPassword(request.get("token"), request.get("password"));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        UserDTO user = userService.getCurrentUser();
        return ResponseEntity.ok(user);
    }
} 