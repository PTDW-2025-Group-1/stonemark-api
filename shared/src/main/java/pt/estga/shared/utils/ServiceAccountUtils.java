package pt.estga.shared.utils;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.shared.models.ServiceAccountPrincipal;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * Utility for running code with a ServiceAccountPrincipal in the SecurityContext.
 */
public final class ServiceAccountUtils {

    private ServiceAccountUtils() {}

    /**
     * Runs a Callable with a ServiceAccountPrincipal set in SecurityContext.
     * Clears the context after execution.
     *
     * @param principal bot or service account principal
     * @param telegramUserId optional Telegram user ID for auditing (can be null)
     * @param action the action to execute
     * @param <T> return type
     * @return result of action
     * @throws Exception rethrows any exception from action
     */
    public static <T> T runAsServiceAccount(ServiceAccountPrincipal principal,
                                            Long telegramUserId,
                                            Callable<T> action) throws Exception {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_BOT"))
                );

        if (telegramUserId != null) {
            auth.setDetails(telegramUserId);
        }

        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            return action.call();
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}
