package services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.auth.services.verification.commands.VerificationCommand;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.repositories.UserIdentityRepository;
import pt.estga.user.service.UserService;

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
class AuthenticationServiceImplTest {

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
    @Mock
    private VerificationInitiationService verificationInitiationService;
    @Mock
    private VerificationCommandFactory verificationCommandFactory;
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private VerificationProcessingService verificationProcessingService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserIdentityRepository userIdentityRepository;
    @Mock
    private TwoFactorAuthenticationService twoFactorAuthenticationService;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User testUser;
    private User disabledTestUser;
    private User tfaEnabledUser;
    private final String testEmail = "test@example.com";
    private final String tfaSecret = "TFA_SECRET_KEY";

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
                .tfaEnabled(false)
                .tfaSecret(null)
                .build();
        UserContact testUserContact = UserContact.builder()
                .id(1L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .primary(true)
                .verified(true)
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
                .tfaEnabled(false)
                .tfaSecret(null)
                .build();
        UserContact disabledTestUserContact = UserContact.builder()
                .id(2L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .primary(true)
                .verified(false)
                .user(disabledTestUser)
                .build();
        disabledTestUser.setContacts(new ArrayList<>(List.of(disabledTestUserContact)));

        tfaEnabledUser = User.builder()
                .id(3L)
                .username("tfa@example.com")
                .password("password")
                .firstName("Tfa")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaEnabled(true)
                .tfaSecret(tfaSecret)
                .build();
        UserContact tfaUserContact = UserContact.builder()
                .id(3L)
                .type(ContactType.EMAIL)
                .value("tfa@example.com")
                .primary(true)
                .verified(true)
                .user(tfaEnabledUser)
                .build();
        tfaEnabledUser.setContacts(new ArrayList<>(List.of(tfaUserContact)));
    }

    // Helper method to set the private @Value field via reflection
    private void setEmailVerificationRequired(boolean value) {
        try {
            Field field = AuthenticationServiceSpringImpl.class.getDeclaredField("emailVerificationRequired");
            field.setAccessible(true);
            field.set(authenticationService, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set emailVerificationRequired field via reflection", e);
        }
    }

    @Test
    @DisplayName("Should register user and require email verification when enabled")
    void testRegister_success_emailVerificationRequired() {
        // Given
        when(userService.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(testUser);
        when(verificationCommandFactory.createEmailVerificationCommand(any(User.class)))
                .thenReturn(mock(VerificationCommand.class));

        // Simulate emailVerificationRequired = true
        setEmailVerificationRequired(true);

        // When
        assertThatExceptionOfType(EmailVerificationRequiredException.class)
                .isThrownBy(() -> authenticationService.register(testUser));

        // Then
        verify(userService).existsByEmail(testEmail);

        // Use ArgumentCaptor to verify the User object passed to userService.create
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isFalse(); // User should be disabled initially

        verify(verificationCommandFactory).createEmailVerificationCommand(any(User.class));
        verify(verificationInitiationService).initiate(any(VerificationCommand.class));
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService); // No tokens generated yet
    }

    @Test
    @DisplayName("Should register user and return tokens when email verification is not required")
    void testRegister_success_noEmailVerificationRequired() {
        // Given
        when(userService.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(testUser);
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        // Simulate emailVerificationRequired = false
        setEmailVerificationRequired(false);

        // When
        Optional<AuthenticationResponseDto> optionalResponse = authenticationService.register(testUser);

        // Then
        assertThat(optionalResponse).isPresent();
        AuthenticationResponseDto response = optionalResponse.get();
        assertThat(response.accessToken()).isEqualTo("accessTokenString");
        assertThat(response.refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.tfaEnabled()).isFalse();
        assertThat(response.tfaRequired()).isFalse();

        verify(userService).existsByEmail(testEmail);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isTrue(); // User should be enabled immediately

        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(verificationCommandFactory, verificationInitiationService); // No verification initiated
    }

    @Test
    @DisplayName("Should throw EmailAlreadyTakenException if email exists")
    void testRegister_emailAlreadyTaken() {
        // Given
        when(userService.existsByEmail(testEmail)).thenReturn(true);

        // When
        assertThatExceptionOfType(EmailAlreadyTakenException.class)
                .isThrownBy(() -> authenticationService.register(testUser));

        // Then
        verify(userService).existsByEmail(testEmail);
        verifyNoMoreInteractions(userService); // No further calls to userService
        verifyNoInteractions(verificationCommandFactory, verificationInitiationService);
    }

    @Test
    @DisplayName("Should authenticate user successfully when email verification is not required and 2FA is not enabled")
    void testAuthenticate_success_noEmailVerificationRequired_noTfa() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        // Simulate emailVerificationRequired = false
        setEmailVerificationRequired(false);

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(testEmail, testUser.getPassword(), null);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isFalse();
        assertThat(response.get().tfaRequired()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(testEmail);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return 2FA required when 2FA is enabled but no code is provided")
    void testAuthenticate_tfaEnabled_tfaCodeRequired() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(tfaEnabledUser.getUsername())).thenReturn(Optional.of(tfaEnabledUser));

        // Simulate emailVerificationRequired = false
        setEmailVerificationRequired(false);

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUser.getUsername(), tfaEnabledUser.getPassword(), null);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isNull(); // No access token yet
        assertThat(response.get().refreshToken()).isNull(); // No refresh token yet
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isTrue();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(tfaEnabledUser.getUsername());
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService, twoFactorAuthenticationService); // No tokens generated, no TFA verification yet
    }

    @Test
    @DisplayName("Should return empty optional for invalid 2FA code")
    void testAuthenticate_tfaEnabled_invalidTfaCode() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(tfaEnabledUser.getUsername())).thenReturn(Optional.of(tfaEnabledUser));
        when(twoFactorAuthenticationService.isCodeValid(tfaSecret, "invalidCode")).thenReturn(false);

        // Simulate emailVerificationRequired = false
        setEmailVerificationRequired(false);

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUser.getUsername(), tfaEnabledUser.getPassword(), "invalidCode");

        // Then
        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(tfaEnabledUser.getUsername());
        verify(twoFactorAuthenticationService).isCodeValid(tfaSecret, "invalidCode");
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService); // No tokens generated
    }

    @Test
    @DisplayName("Should authenticate successfully with valid 2FA code")
    void testAuthenticate_tfaEnabled_validTfaCode() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(tfaEnabledUser.getUsername())).thenReturn(Optional.of(tfaEnabledUser));
        when(twoFactorAuthenticationService.isCodeValid(tfaSecret, "validCode")).thenReturn(true);
        when(jwtService.generateRefreshToken(tfaEnabledUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(tfaEnabledUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        // Simulate emailVerificationRequired = false
        setEmailVerificationRequired(false);

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUser.getUsername(), tfaEnabledUser.getPassword(), "validCode");

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(tfaEnabledUser.getUsername());
        verify(twoFactorAuthenticationService).isCodeValid(tfaSecret, "validCode");
        verify(jwtService).generateRefreshToken(tfaEnabledUser);
        verify(jwtService).generateAccessToken(tfaEnabledUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
    }


    @Test
    @DisplayName("Should throw EmailVerificationRequiredException when email verification is required and user is not enabled")
    void testAuthenticate_emailVerificationRequired_userNotEnabled() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(disabledTestUser)); // User is disabled

        // Simulate emailVerificationRequired = true
        setEmailVerificationRequired(true);

        // When
        assertThatExceptionOfType(EmailVerificationRequiredException.class)
                .isThrownBy(() -> authenticationService.authenticate(testEmail, disabledTestUser.getPassword(), null));

        // Then
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(testEmail);
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService, twoFactorAuthenticationService); // No tokens generated
    }

    @Test
    @DisplayName("Should authenticate user successfully when email verification is required and user is enabled")
    void testAuthenticate_emailVerificationRequired_userEnabled() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser)); // User is enabled

        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        // Simulate emailVerificationRequired = true
        setEmailVerificationRequired(true);

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(testEmail, testUser.getPassword(), null);

        // Then
        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isFalse();
        assertThat(response.get().tfaRequired()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByEmail(testEmail);
        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return empty optional for invalid credentials")
    void testAuthenticate_invalidCredentials() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(mock(AuthenticationException.class));

        // When
        Optional<AuthenticationResponseDto> response = authenticationService.authenticate("test@example.com", "wrongpassword", null);

        // Then
        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verifyNoInteractions(userService, jwtService, accessTokenService, refreshTokenService, twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should initiate password reset request")
    void testRequestPasswordReset_success() {
        // Given
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(verificationCommandFactory.createPasswordResetCommand(any(User.class)))
                .thenReturn(mock(VerificationCommand.class));

        // When
        authenticationService.requestPasswordReset(testEmail);

        // Then
        verify(userService).findByEmail(testEmail);
        verify(verificationCommandFactory).createPasswordResetCommand(testUser);
        verify(verificationInitiationService).initiate(any(VerificationCommand.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if user not found for password reset")
    void testRequestPasswordReset_userNotFound() {
        // Given
        String email = "nonexistent@example.com";
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        // When
        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> authenticationService.requestPasswordReset(email))
                .withMessage("User not found");

        // Then
        verify(userService).findByEmail(email);
        verifyNoInteractions(verificationCommandFactory, verificationInitiationService);
    }

    @Test
    @DisplayName("Should delegate password reset to verification processing service")
    void testResetPassword_success() {
        // Given
        String token = "validToken";
        String newPassword = "newPassword";

        // When
        authenticationService.resetPassword(token, newPassword);

        // Then
        verify(verificationProcessingService).processPasswordReset(token, newPassword);
    }
}
