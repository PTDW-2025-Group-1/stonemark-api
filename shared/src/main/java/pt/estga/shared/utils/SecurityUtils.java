package pt.estga.shared.utils;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.shared.models.AuthenticatedPrincipal;
import pt.estga.shared.models.ServiceAccountPrincipal;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public final class SecurityUtils {

    private SecurityUtils() {}

    /**
     * Retrieves the ID of the currently authenticated user from the SecurityContext.
     *
     * @return An Optional containing the user ID if authenticated, or empty if not.
     */
    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof AuthenticatedPrincipal authenticatedPrincipal) {
            return Optional.of(authenticatedPrincipal.getId());
        }

        return Optional.empty();
    }
}
