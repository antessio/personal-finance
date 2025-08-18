package antessio.personalfinance.infrastructure.web.controller.authentication;


import java.util.Set;
import java.util.stream.Collectors;

import antessio.personalfinance.infrastructure.security.persistence.User;
import antessio.personalfinance.infrastructure.security.service.SecurityUtils;
import antessio.personalfinance.infrastructure.web.controller.authentication.dto.UserDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import antessio.personalfinance.infrastructure.security.persistence.Role;
import antessio.personalfinance.infrastructure.security.service.AuthenticationService;
import antessio.personalfinance.infrastructure.web.controller.authentication.dto.LoginUserRequest;
import antessio.personalfinance.infrastructure.web.controller.authentication.dto.RegisterUserRequest;

@Controller
public class UserController {

    private final AuthenticationService authenticationService;
    private final SecurityUtils securityUtils;

    public UserController(AuthenticationService authenticationService, SecurityUtils securityUtils) {
        this.authenticationService = authenticationService;
        this.securityUtils = securityUtils;
    }


    @GetMapping("/api/users/me")
    public ResponseEntity<UserDTO> getCurrentUser() {
        User user = securityUtils.getAuthenticatedUser();
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new UserDTO(user.getFullName(), user.getEmail(),
                user.getRoles().stream().map(Role::getRole).map(Enum::name).collect(Collectors.toSet())));
    }

    @PostMapping("/public/api/users/register")
    public ResponseEntity<Void> registerUser(@RequestBody RegisterUserRequest registerUserRequest) {
        // Validate input
        if (registerUserRequest == null) {
            throw new IllegalArgumentException("RegisterUserDTO cannot be null");
        }

        authenticationService.signIn(
                registerUserRequest.username(),
                registerUserRequest.password(),
                registerUserRequest.firstName() + " " + registerUserRequest.lastName(),
                true, // Assuming isVerified is true for simplicity
                Set.of(Role.RoleType.ADMIN)); // Default role for new users
        return ResponseEntity.ok().build();

    }

    @PostMapping("/public/api/users/login")
    public ResponseEntity<String> loginUser(@RequestBody LoginUserRequest loginUserRequest) {
        // Validate input
        if (loginUserRequest == null) {
            throw new IllegalArgumentException("LoginUserDTO cannot be null");
        }

        String userToken = authenticationService.authenticate(loginUserRequest.username(), loginUserRequest.password());
        return ResponseEntity.ok(userToken);
    }





}
