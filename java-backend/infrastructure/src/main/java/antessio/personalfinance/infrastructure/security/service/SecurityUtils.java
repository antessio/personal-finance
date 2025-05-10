package antessio.personalfinance.infrastructure.security.service;

import antessio.personalfinance.infrastructure.security.persistence.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
