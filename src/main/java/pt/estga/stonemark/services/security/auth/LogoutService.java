package pt.estga.stonemark.services.security.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.RefreshToken;
import pt.estga.stonemark.services.security.token.AccessTokenService;
import pt.estga.stonemark.services.security.token.RefreshTokenService;

@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {

    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;

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

        accessTokenService.findByToken(jwtToken).ifPresent(token -> {
            accessTokenService.revokeToken(jwtToken);

            RefreshToken refreshToken = token.getRefreshToken();
            if (refreshToken != null) {
                refreshTokenService.revokeToken(refreshToken);
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
