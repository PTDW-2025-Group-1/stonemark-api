package pt.estga.auth.services;

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
import pt.estga.security.entities.RefreshToken;
import pt.estga.security.services.AccessTokenService;
import pt.estga.security.services.JwtService;
import pt.estga.security.services.RefreshTokenService;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TotpService;
import pt.estga.shared.exceptions.UsernameAlreadyTakenException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private UserContactService userContactService;

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
    private TotpService totpService;

    @Mock
    private TwoFactorAuthenticationService twoFactorAuthenticationService;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User user;
    private UserContact userContact;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFirstName("Test");
        user.setLastName("User");
        user.setPassword("password");
        user.setUsername("user228");
        user.setRole(Role.USER);

        userContact = new UserContact();
        userContact.setUser(user);
        userContact.setValue("test@example.com");
    }

    @Test
    void register_shouldCreateUserAndReturnAuthResponse_whenGivenValidData() {
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(user);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.register(user);

        assertTrue(response.isPresent());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());

        verify(userService).create(
                argThat(u ->
                        u.getUsername().equals("user228") &&
                        u.getFirstName().equals("Test") &&
                        u.getLastName().equals("User") &&
                        u.getPassword().equals("encodedPassword"))
        );
    }

    @Test
    void register_shouldThrowUsernameAlreadyTakenException_whenUsernameExists() {
        when(userService.existsByUsername(user.getUsername())).thenReturn(true);

        assertThrows(UsernameAlreadyTakenException.class, () -> {
            authenticationService.register(user);
        });

        verify(userService, never()).create(any(User.class));
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenCredentialsAreValid() {
        user.setTfaMethod(TfaMethod.NONE);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
        assertFalse(response.get().tfaRequired());
        assertFalse(response.get().tfaCodeSent());
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
    void authenticate_shouldReturnTfaRequired_whenTfaTotpIsEnabledAndNoCodeProvided() {
        user.setTfaMethod(TfaMethod.TOTP);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertTrue(response.get().tfaRequired());
        assertFalse(response.get().tfaCodeSent());
        assertNull(response.get().accessToken());
        assertNull(response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnTfaRequiredAndCodeSent_whenTfaSmsIsEnabledAndNoCodeProvided() {
        user.setTfaMethod(TfaMethod.SMS);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertTrue(response.get().tfaRequired());
        assertTrue(response.get().tfaCodeSent());
        assertNull(response.get().accessToken());
        assertNull(response.get().refreshToken());
        verify(twoFactorAuthenticationService).generateAndSendSmsCode(user);
    }

    @Test
    void authenticate_shouldReturnTfaRequiredAndCodeSent_whenTfaEmailIsEnabledAndNoCodeProvided() {
        user.setTfaMethod(TfaMethod.EMAIL);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", null);

        assertTrue(response.isPresent());
        assertTrue(response.get().tfaRequired());
        assertTrue(response.get().tfaCodeSent());
        assertNull(response.get().accessToken());
        assertNull(response.get().refreshToken());
        verify(twoFactorAuthenticationService).generateAndSendEmailCode(user);
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenTfaTotpIsEnabledAndCodeIsValid() {
        user.setTfaMethod(TfaMethod.TOTP);
        user.setTfaSecret("secret");

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(totpService.isCodeValid("secret", "123456")).thenReturn(true);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "123456");

        assertTrue(response.isPresent());
        assertFalse(response.get().tfaRequired());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenTfaSmsIsEnabledAndCodeIsValid() {
        user.setTfaMethod(TfaMethod.SMS);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(twoFactorAuthenticationService.verifyCode(user, "123456", ActionCodeType.TWO_FACTOR)).thenReturn(true);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "123456");

        assertTrue(response.isPresent());
        assertFalse(response.get().tfaRequired());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnAuthenticationResponse_whenTfaEmailIsEnabledAndCodeIsValid() {
        user.setTfaMethod(TfaMethod.EMAIL);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(twoFactorAuthenticationService.verifyCode(user, "123456", ActionCodeType.TWO_FACTOR)).thenReturn(true);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(user.getUsername())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "123456");

        assertTrue(response.isPresent());
        assertFalse(response.get().tfaRequired());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
    }

    @Test
    void authenticate_shouldReturnEmpty_whenTfaTotpIsEnabledAndCodeIsInvalid() {
        user.setTfaMethod(TfaMethod.TOTP);
        user.setTfaSecret("secret");

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(totpService.isCodeValid("secret", "wrong-code")).thenReturn(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "wrong-code");

        assertFalse(response.isPresent());
    }

    @Test
    void authenticate_shouldReturnEmpty_whenTfaSmsIsEnabledAndCodeIsInvalid() {
        user.setTfaMethod(TfaMethod.SMS);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(twoFactorAuthenticationService.verifyCode(user, "wrong-code", ActionCodeType.TWO_FACTOR)).thenReturn(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "wrong-code");

        assertFalse(response.isPresent());
    }

    @Test
    void authenticate_shouldReturnEmpty_whenTfaEmailIsEnabledAndCodeIsInvalid() {
        user.setTfaMethod(TfaMethod.EMAIL);

        when(userContactService.findByValue("test@example.com")).thenReturn(Optional.of(userContact));
        when(twoFactorAuthenticationService.verifyCode(user, "wrong-code", ActionCodeType.TWO_FACTOR)).thenReturn(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "password", "wrong-code");

        assertFalse(response.isPresent());
    }

    @Test
    void refreshToken_shouldReturnNewAccessToken_whenRefreshTokenIsValid() {
        String refreshTokenString = "validRefreshToken";
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(refreshTokenString);

        when(refreshTokenService.findByToken(refreshTokenString)).thenReturn(Optional.of(refreshToken));
        when(jwtService.isTokenValid(refreshTokenString, user.getUsername())).thenReturn(true);
        when(jwtService.generateAccessToken(user.getUsername())).thenReturn("newAccessToken");

        Optional<AuthenticationResponseDto> response = authenticationService.refreshToken(refreshTokenString);

        assertTrue(response.isPresent());
        assertEquals("newAccessToken", response.get().accessToken());
        assertEquals(refreshTokenString, response.get().refreshToken());
        assertFalse(response.get().tfaRequired());
        assertFalse(response.get().tfaCodeSent());
        assertEquals(user.getRole().name(), response.get().role());
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
