package antessio.personalfinance.infrastructure.web.controller;


import antessio.personalfinance.infrastructure.security.persistence.Role;
import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.AuthenticationService;
import antessio.personalfinance.infrastructure.web.controller.dto.AuthRequest;
import antessio.personalfinance.infrastructure.web.controller.dto.SignInRequest;
import antessio.personalfinance.infrastructure.web.controller.dto.TokenDTO;
import antessio.personalfinance.infrastructure.web.controller.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> login(@RequestBody AuthRequest loginRequest) {
        return ResponseEntity.ok(new TokenDTO(authenticationService.authenticate(loginRequest.username(), loginRequest.password())));
    }

    @PostMapping("/sign_in")
    public ResponseEntity<Void> signIn(@RequestBody SignInRequest signInRequest) {
        boolean isVerified = true;

        Set<String> roles = new HashSet<>();

        authenticationService.signIn(signInRequest.username(), signInRequest.password(), signInRequest.fullName(), isVerified, roles);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user")
    public ResponseEntity<UserDTO> user() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = (User) authentication.getPrincipal();

        return ResponseEntity.ok(UserDTO.builder()
                                        .id(user.getId().toString())
                                        .email(user.getEmail())
                                        .fullName(user.getFullName())
                                        .roles(Optional.ofNullable(user.getRoles())
                                                       .orElseGet(Collections::emptySet)
                                                       .stream()
                                                       .map(Role::getRole)
                                                       .collect(Collectors.toSet()))
                                        .build());
    }


}
