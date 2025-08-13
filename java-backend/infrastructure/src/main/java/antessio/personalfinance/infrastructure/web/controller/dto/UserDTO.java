package antessio.personalfinance.infrastructure.web.controller.dto;

import lombok.Builder;

import java.util.Set;

@Builder
public class UserDTO {
    private String id;
    private String email;
    private String fullName;
    private Set<String> roles;
}
