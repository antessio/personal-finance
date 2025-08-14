package antessio.personalfinance.infrastructure.security.service;

import antessio.personalfinance.infrastructure.security.persistence.Role;
import antessio.personalfinance.infrastructure.security.persistence.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

@Component
public class SecurityUtils {
    @Value("${security.authentication.enabled:true}")
    private boolean authenticationEnabled;

    public User getAuthenticatedUser() {
        if (!this.authenticationEnabled) {
            User admin = new User();
            admin.setId(UUID.randomUUID());
            admin.setEmail("admin");
            admin.setRoles(Set.of(Role.builder()
                    .id(UUID.randomUUID())
                    .role(Role.RoleType.ADMIN)
                    .build()));
            return admin;
        } else {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return (User) authentication.getPrincipal();
            }

        }
        return null;
    }
}
