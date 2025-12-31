package pt.estga.shared.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.shared.models.AppPrincipal;

import java.util.concurrent.Callable;

/**
 * Utility for running code with a ServiceAccountPrincipal in the SecurityContext.
 */
public final class ServiceAccountUtils {

    private ServiceAccountUtils() {}

    public static <T> T runAsServiceAccount(AppPrincipal servicePrincipal, Callable<T> action) throws Exception {
        Authentication auth =
                new UsernamePasswordAuthenticationToken(
                        servicePrincipal,
                        null,
                        servicePrincipal.getAuthorities()
                );

        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            return action.call();
        } finally {
            SecurityContextHolder.clearContext();
        }

    }
}
