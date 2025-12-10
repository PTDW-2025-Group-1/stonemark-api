package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.entities.token.RefreshToken;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private TwoFactorAuthenticationService twoFactorAuthenticationService;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password");
        user.setUsername("test@example.com");
        user.setRole(Role.USER);
        UserContact email = new UserContact();
        email.setType(ContactType.EMAIL);
        email.setValue("test@example.com");
        email.setPrimary(true);
        user.setContacts(List.of(email));
    }

    @Test
    void register_shouldCreateUser_whenGivenValidData() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(user);

        authenticationService.register(user);

        verify(userService).create(
                argThat(u -> u.getFirstName().equals("Test") &&
                        u.getLastName().equals("User") &&
                        u.getContacts().stream().anyMatch(c -> c.getValue().equals("test@example.com") && c.isPrimary()) &&
                        u.getPassword().equals("encodedPassword"))
        );
    }

    @Test
    void register_shouldThrowEmailAlreadyTakenException_whenEmailIsInUse() {
        when(userService.existsByContactValue("test@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyTakenException.class, () -> authenticationService.register(user));
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenCredentialsAreValid() {
        user.setTfaMethod(TfaMethod.NONE);

        when(userService.findByContact("test@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
        verify(authenticationManager).authenticate(any());
        verify(accessTokenService).createToken(any(), any(), any());
        verify(refreshTokenService).createToken(any(), any());
    }

    @Test
    void authenticate_shouldReturnEmpty_whenCredentialsAreInvalid() {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "wrongpassword", null);

        assertFalse(response.isPresent());
    }

    @Test
    void authenticate_shouldReturnTfaRequired_whenTfaIsEnabledAndNoCodeProvided() {
        user.setTfaMethod(TfaMethod.TOTP);

        when(userService.findByContact("test@example.com")).thenReturn(Optional.of(user));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertTrue(response.get().tfaRequired());
        assertNull(response.get().accessToken());
        assertNull(response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenTfaIsEnabledAndCodeIsValid() {
        user.setTfaMethod(TfaMethod.TOTP);
        user.setTfaSecret("secret");

        when(userService.findByContact("test@example.com")).thenReturn(Optional.of(user));
        when(twoFactorAuthenticationService.isCodeValid("secret", "123456")).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user)).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "123456");

        assertTrue(response.isPresent());
        assertFalse(response.get().tfaRequired());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnEmpty_whenTfaIsEnabledAndCodeIsInvalid() {
        user.setTfaMethod(TfaMethod.TOTP);
        user.setTfaSecret("secret");

        when(userService.findByContact("test@example.com")).thenReturn(Optional.of(user));
        when(twoFactorAuthenticationService.isCodeValid("secret", "wrong-code")).thenReturn(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "wrong-code");

        assertFalse(response.isPresent());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {
        String refreshTokenString = "validRefreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(jwtService.isTokenValid(refreshTokenString, user)).thenReturn(true);
        when(jwtService.generateAccessToken(user)).thenReturn("newAccessToken");

        Optional<AuthenticationResponseDto> response = authenticationService.refreshToken(refreshTokenString);

        assertTrue(response.isPresent());
        assertEquals("newAccessToken", response.get().accessToken());
        assertEquals(refreshTokenString, response.get().refreshToken());
        verify(accessTokenService).revokeAllByRefreshToken(refreshToken);
        verify(accessTokenService).createToken(user.getUsername(), "newAccessToken", refreshToken);
    }

    @Test
    void refreshToken_shouldReturnEmpty_whenRefreshTokenIsInvalid() {
        String refreshTokenString = "invalidRefreshToken";

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.empty());

        Optional<AuthenticationResponseDto> response = authenticationService.refreshToken(refreshTokenString);

        assertFalse(response.isPresent());
    }

    @Test
    void logoutFromAllDevices_shouldRevokeAllTokensForUser() {
        authenticationService.logoutFromAllDevices(user);

        verify(refreshTokenService).revokeAllByUser(user);
        verify(accessTokenService).revokeAllByUser(user);
    }
}
