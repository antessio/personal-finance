package antessio.personalfinance.infrastructure.web.controller.dto;

public record SignInRequest(String username, String password, String fullName) {
}
