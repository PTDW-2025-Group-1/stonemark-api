package services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import pt.estga.auth.entities.token.AccessToken;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.auth.services.LogoutService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutServiceTest {

    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private HttpSession session;

    @InjectMocks
    private LogoutService logoutService;

    @BeforeEach
    void setUp() {
        // Clear SecurityContextHolder before each test to ensure a clean state
        SecurityContextHolder.clearContext();
    }

    @Test
    void logout_shouldRevokeTokensAndClearContext_whenValidTokenProvided() {
        // Given
        String jwtToken = "valid.jwt.token";
        String authHeader = "Bearer " + jwtToken;
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jwtToken);
        RefreshToken refreshToken = new RefreshToken();
        accessToken.setRefreshToken(refreshToken);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.of(accessToken));
        when(request.getSession(false)).thenReturn(session);

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(accessTokenService, times(1)).revokeToken(jwtToken);
        verify(refreshTokenService, times(1)).revokeToken(refreshToken);
        verify(session, times(1)).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_shouldDoNothing_whenNoAuthorizationHeader() {
        // Given
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
        verify(request, never()).getSession(anyBoolean());
        assertNull(SecurityContextHolder.getContext().getAuthentication()); // Should still be null as no auth was set
    }

    @Test
    void logout_shouldDoNothing_whenAuthorizationHeaderIsNotBearer() {
        // Given
        when(request.getHeader("Authorization")).thenReturn("Basic somecredentials");

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verifyNoInteractions(accessTokenService);
        verifyNoInteractions(refreshTokenService);
        verify(request, never()).getSession(anyBoolean());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_shouldRevokeAccessTokenOnly_whenNoRefreshTokenAssociated() {
        // Given
        String jwtToken = "valid.jwt.token";
        String authHeader = "Bearer " + jwtToken;
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jwtToken); // No refresh token set

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.of(accessToken));
        when(request.getSession(false)).thenReturn(null); // No session to invalidate

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(accessTokenService, times(1)).revokeToken(jwtToken);
        verifyNoInteractions(refreshTokenService); // RefreshTokenService should not be called
        verify(session, never()).invalidate(); // Session should not be invalidated
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void logout_shouldClearContextButNotRevokeTokens_whenTokenNotFound() {
        // Given
        String jwtToken = "nonexistent.jwt.token";
        String authHeader = "Bearer " + jwtToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);

        // When
        logoutService.logout(request, response, authentication);

        // Then
        verify(accessTokenService, never()).revokeToken(anyString());
        verifyNoInteractions(refreshTokenService);
        verify(session, times(1)).invalidate();
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }
}
