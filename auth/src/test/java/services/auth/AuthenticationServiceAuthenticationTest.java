package services.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceAuthenticationTest {

    @Mock
    private UserService userService;
    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User testUser;
    private User disabledTestUser;
    private final String testEmail = "test@example.com";


    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(testEmail)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaMethod(TfaMethod.NONE)
                .tfaSecret(null)
                .build();
        UserContact testUserContact = UserContact.builder()
                .id(1L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .isPrimary(true)
                .isVerified(true)
                .user(testUser)
                .build();
        testUser.setContacts(new ArrayList<>(List.of(testUserContact)));

        disabledTestUser = User.builder()
                .id(1L)
                .username(testEmail)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .enabled(false)
                .tfaMethod(TfaMethod.NONE)
                .tfaSecret(null)
                .build();
        UserContact disabledTestUserContact = UserContact.builder()
                .id(2L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .isPrimary(true)
                .isVerified(false)
                .user(disabledTestUser)
                .build();
        disabledTestUser.setContacts(new ArrayList<>(List.of(disabledTestUserContact)));
    }

    private void setContactVerificationRequired(boolean value) {
        try {
            Field field = AuthenticationServiceSpringImpl.class.getDeclaredField("contactVerificationRequired");
            field.setAccessible(true);
            field.set(authenticationService, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set contactVerificationRequired field via reflection", e);
        }
    }

    @Test
    @DisplayName("Should authenticate user successfully when email verification is not required and 2FA is not enabled")
    void testAuthenticate_success_noEmailVerificationRequired_noTfa() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(testEmail)).thenReturn(Optional.of(testUser));

        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(testEmail, testUser.getPassword(), null);

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isFalse();
        assertThat(response.get().tfaRequired()).isFalse();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(testEmail);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should throw EmailVerificationRequiredException when email verification is required and user is not enabled")
    void testAuthenticate_emailVerificationRequired_userNotEnabled() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(testEmail)).thenReturn(Optional.of(disabledTestUser));

        setContactVerificationRequired(true);

        assertThatExceptionOfType(EmailVerificationRequiredException.class)
                .isThrownBy(() -> authenticationService.authenticate(testEmail, disabledTestUser.getPassword(), null));

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(testEmail);
    }

    @Test
    @DisplayName("Should authenticate user successfully when email verification is required and user is enabled")
    void testAuthenticate_emailVerificationRequired_userEnabled() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(testEmail)).thenReturn(Optional.of(testUser));

        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(true);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(testEmail, testUser.getPassword(), null);

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isFalse();
        assertThat(response.get().tfaRequired()).isFalse();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(testEmail);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
    }

    @Test
    @DisplayName("Should return empty optional for invalid credentials")
    void testAuthenticate_invalidCredentials() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(mock(AuthenticationException.class));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "wrongpassword", null);

        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userService, jwtService, accessTokenService, refreshTokenService);
    }
}
