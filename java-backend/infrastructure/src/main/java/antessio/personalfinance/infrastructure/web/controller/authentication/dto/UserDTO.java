package antessio.personalfinance.infrastructure.web.controller.authentication.dto;

import java.util.Set;

public record UserDTO(String fullName, String email, Set<String> roles) {
}
