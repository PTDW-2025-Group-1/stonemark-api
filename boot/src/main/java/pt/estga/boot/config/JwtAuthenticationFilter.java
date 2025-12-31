package pt.estga.boot.config;

import io.micrometer.common.lang.NonNull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import pt.estga.security.enums.TokenType;
import pt.estga.security.services.JwtService;
import pt.estga.shared.models.AppPrincipal;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AppPrincipalFactory principalFactory;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7);

        // skip if already authenticated
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check if it's a refresh token request
        boolean isRefreshRequest = request.getRequestURI().endsWith("/refresh");
        TokenType expectedType = isRefreshRequest ? TokenType.REFRESH : TokenType.ACCESS;

        // validate token
        if (!jwtService.isTokenValid(token, expectedType)) {
            filterChain.doFilter(request, response);
            return;
        }

        // parse principal info
        try {
            var principalType = jwtService.getPrincipalType(token);
            var principalId = jwtService.getPrincipalId(token);
            var identifier = jwtService.getSubject(token);
            var authorities = jwtService.getAuthorities(token);

            AppPrincipal principal;

            switch (principalType) {
                case USER -> principal = principalFactory.fromJwtUser(principalId, identifier, authorities);
                case SERVICE -> principal = principalFactory.fromJwtService(principalId, identifier, authorities);
                default -> throw new IllegalStateException("Unsupported principal type: " + principalType);
            }

            var authToken = new UsernamePasswordAuthenticationToken(
                    principal,
                    null,
                    principal.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (Exception e) {
            log.error("Could not set user authentication in security context", e);
        }

        filterChain.doFilter(request, response);
    }
}
