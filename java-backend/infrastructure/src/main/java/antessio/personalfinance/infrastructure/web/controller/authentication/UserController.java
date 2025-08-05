package antessio.personalfinance.infrastructure.web.controller.authentication;


import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import antessio.personalfinance.infrastructure.security.persistence.Role;
import antessio.personalfinance.infrastructure.security.service.AuthenticationService;
import antessio.personalfinance.infrastructure.web.controller.authentication.dto.LoginUserRequest;
import antessio.personalfinance.infrastructure.web.controller.authentication.dto.RegisterUserRequest;

@Controller
public class UserController {

    private final AuthenticationService authenticationService;

    public UserController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    // register user
    // login user

    @PostMapping("/public/api/users/register")
    public void registerUser(@RequestBody RegisterUserRequest registerUserRequest) {
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

    }

    @PostMapping("/public/api/users/login")
    public String loginUser(@RequestBody LoginUserRequest loginUserRequest) {
        // Validate input
        if (loginUserRequest == null) {
            throw new IllegalArgumentException("LoginUserDTO cannot be null");
        }

        return authenticationService.authenticate(loginUserRequest.username(), loginUserRequest.password());
    }





}
