package pt.estga.stonemark.services.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final TokenService tokenService;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("No Authorization header or not a Bearer token");
            return;
        }

        final String jwtToken = authHeader.substring(7).trim();

        tokenService.findByToken(jwtToken).ifPresent(token -> {
            // Revoke access token first
            tokenService.revoke(jwtToken);

            String refreshToken = token.getRefreshToken();
            if (refreshToken != null && !refreshToken.isBlank()) {
                // Revoke all tokens associated with this refresh token and the refresh token itself.
                tokenService.revokeAllByRefreshToken(refreshToken);
                tokenService.revoke(refreshToken);
            }

            log.debug("Revoked tokens for token id: {}", token.getId());
        });

        // Invalidate session if present
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        SecurityContextHolder.clearContext();
    }
}
