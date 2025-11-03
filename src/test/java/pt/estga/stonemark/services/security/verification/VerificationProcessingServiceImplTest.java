package pt.estga.stonemark.services.security.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.stonemark.dtos.auth.ConfirmationResponseDto;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.ConfirmationStatus;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.exceptions.SamePasswordException;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.processing.PasswordResetConfirmationProcessor;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessor;
import pt.estga.stonemark.services.security.verification.processing.VerificationProcessorFactory;
import pt.estga.stonemark.exceptions.VerificationErrorMessages;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT) // Add this line to allow unnecessary stubbings
class VerificationProcessingServiceImplTest {

    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private VerificationProcessorFactory verificationProcessorFactory;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private VerificationProcessor mockProcessor; // Generic mock for other processors
    @Mock
    private PasswordResetConfirmationProcessor passwordResetConfirmationProcessor; // New mock for this specific processor

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    @Mock // testUser itself should be a mock if we want to verify interactions with it
    private User testUser;
    private VerificationToken emailVerificationToken;
    private VerificationToken passwordResetToken;

    @BeforeEach
    void setUp() {
        // Configure the mock testUser
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getPassword()).thenReturn("oldHashedPassword"); // Initial password for the mock
        when(testUser.isEnabled()).thenReturn(false);

        emailVerificationToken = VerificationToken.builder()
                .token("uuid-email-verify")
                .code("ABCDEF")
                .user(testUser) // This token refers to the mock user
                .purpose(VerificationTokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        passwordResetToken = VerificationToken.builder()
                .token("uuid-password-reset")
                .code("123456")
                .user(testUser) // This token refers to the mock user
                .purpose(VerificationTokenPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("Should confirm email verification token successfully")
    void testProcessTokenConfirmation_emailVerification_success() {
        // Given
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.EMAIL_VERIFICATION))
                .thenReturn(mockProcessor);
        when(mockProcessor.process(emailVerificationToken)).thenReturn(Optional.empty()); // Email verification completes the action

        // When
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation(emailVerificationToken.getToken());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.SUCCESS);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.EMAIL_VERIFICATION);
        verify(mockProcessor).process(emailVerificationToken);
        // Token revocation is handled by the processor itself for non-password-reset purposes
    }

    @Test
    @DisplayName("Should return password reset required status for password reset token confirmation")
    void testProcessTokenConfirmation_passwordReset_success() {
        // Given
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.PASSWORD_RESET))
                .thenReturn(passwordResetConfirmationProcessor); // Use the mock
        when(passwordResetConfirmationProcessor.process(passwordResetToken))
                .thenReturn(Optional.of(passwordResetToken.getToken())); // Define mock behavior

        // When
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation(passwordResetToken.getToken());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.PASSWORD_RESET_REQUIRED);
        assertThat(response.token()).isEqualTo(passwordResetToken.getToken());
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.PASSWORD_RESET);
        verify(passwordResetConfirmationProcessor).process(passwordResetToken); // Verify interaction with mock
        // No token revocation by VerificationProcessingServiceImpl for password reset
    }

    @Test
    @DisplayName("Should return error for invalid token confirmation")
    void testProcessTokenConfirmation_invalidToken() {
        // Given
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        // When
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation("nonexistent");

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.ERROR);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(verificationProcessorFactory, mockProcessor);
    }

    @Test
    @DisplayName("Should return error and revoke expired token confirmation")
    void testProcessTokenConfirmation_expiredToken() {
        // Given
        emailVerificationToken.setExpiresAt(Instant.now().minusSeconds(10)); // Make token expired
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        // When
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation(emailVerificationToken.getToken());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.ERROR);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(verificationTokenService).revokeToken(emailVerificationToken); // Should revoke expired token
        verifyNoInteractions(verificationProcessorFactory, mockProcessor);
    }

    @Test
    @DisplayName("Should return error and revoke revoked token confirmation")
    void testProcessTokenConfirmation_revokedToken() {
        // Given
        emailVerificationToken.setRevoked(true); // Make token revoked
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        // When
        ConfirmationResponseDto response = verificationProcessingService.processTokenConfirmation(emailVerificationToken.getToken());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.ERROR);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any()); // Already revoked, so no further revocation call
        verifyNoInteractions(verificationProcessorFactory, mockProcessor);
    }

    @Test
    @DisplayName("Should confirm email verification code successfully")
    void testProcessCodeConfirmation_emailVerification_success() {
        // Given
        when(verificationTokenService.findByCode(emailVerificationToken.getCode()))
                .thenReturn(Optional.of(emailVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.EMAIL_VERIFICATION))
                .thenReturn(mockProcessor);
        when(mockProcessor.process(emailVerificationToken)).thenReturn(Optional.empty());

        // When
        ConfirmationResponseDto response = verificationProcessingService.processCodeConfirmation(emailVerificationToken.getCode());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.SUCCESS);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.CONFIRMATION_SUCCESSFUL);
        verify(verificationTokenService).findByCode(emailVerificationToken.getCode());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.EMAIL_VERIFICATION);
        verify(mockProcessor).process(emailVerificationToken);
    }

    @Test
    @DisplayName("Should return password reset required status for password reset code confirmation")
    void testProcessCodeConfirmation_passwordReset_success() {
        // Given
        when(verificationTokenService.findByCode(passwordResetToken.getCode()))
                .thenReturn(Optional.of(passwordResetToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.PASSWORD_RESET))
                .thenReturn(passwordResetConfirmationProcessor); // Use the mock
        when(passwordResetConfirmationProcessor.process(passwordResetToken))
                .thenReturn(Optional.of(passwordResetToken.getToken())); // Define mock behavior

        // When
        ConfirmationResponseDto response = verificationProcessingService.processCodeConfirmation(passwordResetToken.getCode());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.PASSWORD_RESET_REQUIRED);
        assertThat(response.token()).isEqualTo(passwordResetToken.getToken());
        verify(verificationTokenService).findByCode(passwordResetToken.getCode());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.PASSWORD_RESET);
        verify(passwordResetConfirmationProcessor).process(passwordResetToken); // Verify interaction with mock
    }

    @Test
    @DisplayName("Should return error and revoke revoked code confirmation")
    void testProcessCodeConfirmation_revokedToken() {
        // Given
        emailVerificationToken.setRevoked(true); // Make token revoked
        when(verificationTokenService.findByCode(emailVerificationToken.getCode()))
                .thenReturn(Optional.of(emailVerificationToken));

        // When
        ConfirmationResponseDto response = verificationProcessingService.processCodeConfirmation(emailVerificationToken.getCode());

        // Then
        assertThat(response.status()).isEqualTo(ConfirmationStatus.ERROR);
        assertThat(response.message()).isEqualTo(VerificationErrorMessages.CODE_REVOKED);
        verify(verificationTokenService).findByCode(emailVerificationToken.getCode());
        verify(verificationTokenService, never()).revokeToken(any()); // Already revoked, so no further revocation call
        verifyNoInteractions(verificationProcessorFactory, mockProcessor);
    }

    @Test
    @DisplayName("Should successfully process password reset")
    void testProcessPasswordReset_success() {
        // Given
        String newHashedPassword = "newHashedPassword";
        String oldPasswordInTestUser = "oldHashedPassword"; // Explicitly define the expected old password

        // Configure mock testUser's password getter for the matches check
        when(testUser.getPassword()).thenReturn(oldPasswordInTestUser);

        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(anyString(), eq(oldPasswordInTestUser))).thenReturn(false); // New password is not the same
        when(passwordEncoder.encode("newPassword")).thenReturn(newHashedPassword);

        // When
        verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword");

        // Then
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(passwordEncoder).matches("newPassword", oldPasswordInTestUser); // Verify with the captured old password
        verify(testUser).setPassword(newHashedPassword); // Verify setPassword was called on the mock user
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(passwordResetToken);
    }

    @Test
    @DisplayName("Should throw SamePasswordException if new password is same as old")
    void testProcessPasswordReset_samePassword() {
        // Given
        String oldPasswordInTestUser = "oldHashedPassword";
        when(testUser.getPassword()).thenReturn(oldPasswordInTestUser);

        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(anyString(), eq(oldPasswordInTestUser))).thenReturn(true); // New password IS the same

        // When
        SamePasswordException exception = assertThrows(SamePasswordException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        // Then
        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.SAME_PASSWORD);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(passwordEncoder).matches("newPassword", oldPasswordInTestUser);
        verifyNoMoreInteractions(userService, verificationTokenService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for wrong token purpose during password reset")
    void testProcessPasswordReset_invalidTokenPurpose() {
        // Given
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken)); // Use an email verification token

        // When
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset(emailVerificationToken.getToken(), "newPassword"));

        // Then
        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token not found during password reset")
    void testProcessPasswordReset_tokenNotFound() {
        // Given
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset("nonexistent", "newPassword"));

        // Then
        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token is expired during password reset")
    void testProcessPasswordReset_expiredToken() {
        // Given
        passwordResetToken.setExpiresAt(Instant.now().minusSeconds(10)); // Make token expired
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        // Then
        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationTokenService).revokeToken(passwordResetToken); // Should revoke expired token
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token is revoked during password reset")
    void testProcessPasswordReset_revokedToken() {
        // Given
        passwordResetToken.setRevoked(true); // Make token revoked
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));

        // When & Then
        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        // Then
        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any()); // Already revoked
        verifyNoInteractions(passwordEncoder, userService);
    }
}
