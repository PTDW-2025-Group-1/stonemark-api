package services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import pt.estga.auth.entities.token.AccessToken;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.auth.services.LogoutService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;

import java.util.Optional;

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

    @Test
    void logout_shouldRevokeTokensAndInvalidateSession_whenValidTokenProvided() {
        String jwtToken = "valid-jwt-token";
        String authHeader = "Bearer " + jwtToken;
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jwtToken);
        RefreshToken refreshToken = new RefreshToken();
        accessToken.setRefreshToken(refreshToken);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.of(accessToken));
        when(request.getSession(false)).thenReturn(session);

        logoutService.logout(request, response, authentication);

        verify(accessTokenService).revokeToken(jwtToken);
        verify(refreshTokenService).revokeToken(refreshToken);
        verify(session).invalidate();
    }

    @ParameterizedTest
    @ValueSource(strings = {"Basic somecredentials", "Invalid-Header", "Bearer"})
    void logout_shouldDoNothing_whenAuthorizationHeaderIsInvalid(String invalidAuthHeader) {
        when(request.getHeader("Authorization")).thenReturn(invalidAuthHeader);

        logoutService.logout(request, response, authentication);

        verifyNoInteractions(accessTokenService, refreshTokenService);
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    void logout_shouldDoNothing_whenNoAuthorizationHeader() {
        when(request.getHeader("Authorization")).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verifyNoInteractions(accessTokenService, refreshTokenService);
        verify(request, never()).getSession(anyBoolean());
    }

    @Test
    void logout_shouldRevokeAccessTokenOnly_whenNoRefreshTokenAssociated() {
        String jwtToken = "valid-jwt-token";
        String authHeader = "Bearer " + jwtToken;
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jwtToken);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.of(accessToken));
        when(request.getSession(false)).thenReturn(null);

        logoutService.logout(request, response, authentication);

        verify(accessTokenService).revokeToken(jwtToken);
        verifyNoInteractions(refreshTokenService);
        verify(session, never()).invalidate();
    }

    @Test
    void logout_shouldInvalidateSession_whenTokenNotFound() {
        String jwtToken = "nonexistent-jwt-token";
        String authHeader = "Bearer " + jwtToken;

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.empty());
        when(request.getSession(false)).thenReturn(session);

        logoutService.logout(request, response, authentication);

        verify(accessTokenService, never()).revokeToken(anyString());
        verifyNoInteractions(refreshTokenService);
        verify(session).invalidate();
    }

    @Test
    void logout_shouldHandleTrimmedToken() {
        String jwtToken = "valid-jwt-token";
        String authHeader = "Bearer " + jwtToken + "   ";
        AccessToken accessToken = new AccessToken();
        accessToken.setToken(jwtToken);

        when(request.getHeader("Authorization")).thenReturn(authHeader);
        when(accessTokenService.findByToken(jwtToken)).thenReturn(Optional.of(accessToken));
        when(request.getSession(false)).thenReturn(session);

        logoutService.logout(request, response, authentication);

        verify(accessTokenService).revokeToken(jwtToken);
        verify(session).invalidate();
    }
}
