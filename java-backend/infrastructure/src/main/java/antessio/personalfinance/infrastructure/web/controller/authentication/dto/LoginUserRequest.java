package antessio.personalfinance.infrastructure.web.controller.authentication.dto;

public record LoginUserRequest(String username, String password) {

    public LoginUserRequest {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be null or blank");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }
    }

}
