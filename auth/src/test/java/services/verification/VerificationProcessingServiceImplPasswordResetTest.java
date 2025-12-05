package services.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.auth.services.verification.processing.PasswordResetConfirmationProcessor;
import pt.estga.auth.services.verification.processing.VerificationProcessorFactory;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.SamePasswordException;
import pt.estga.shared.exceptions.VerificationErrorMessages;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.InvalidVerificationPurposeException;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceImplPasswordResetTest {

    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private VerificationProcessorFactory verificationProcessorFactory;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PasswordResetConfirmationProcessor passwordResetConfirmationProcessor;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    @Mock
    private User testUser;
    private VerificationToken emailVerificationToken;
    private VerificationToken passwordResetToken;

    @BeforeEach
    void setUp() {
        emailVerificationToken = VerificationToken.builder()
                .token("uuid-email-verify")
                .code("ABCDEF")
                .user(testUser)
                .purpose(VerificationTokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        passwordResetToken = VerificationToken.builder()
                .token("uuid-password-reset")
                .code("123456")
                .user(testUser)
                .purpose(VerificationTokenPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("Should return password reset required status for password reset token confirmation")
    void testConfirmToken_passwordReset_success() {
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.PASSWORD_RESET))
                .thenReturn(passwordResetConfirmationProcessor);
        when(passwordResetConfirmationProcessor.process(passwordResetToken))
                .thenReturn(Optional.of(passwordResetToken.getToken()));

        Optional<String> result = verificationProcessingService.confirmToken(passwordResetToken.getToken());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(passwordResetToken.getToken());
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.PASSWORD_RESET);
        verify(passwordResetConfirmationProcessor).process(passwordResetToken);
    }

    @Test
    @DisplayName("Should return password reset required status for password reset code confirmation")
    void testConfirmCode_passwordReset_success() {
        when(verificationTokenService.findByCode(passwordResetToken.getCode()))
                .thenReturn(Optional.of(passwordResetToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.PASSWORD_RESET))
                .thenReturn(passwordResetConfirmationProcessor);
        when(passwordResetConfirmationProcessor.process(passwordResetToken))
                .thenReturn(Optional.of(passwordResetToken.getToken()));

        Optional<String> result = verificationProcessingService.confirmCode(passwordResetToken.getCode());

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(passwordResetToken.getToken());
        verify(verificationTokenService).findByCode(passwordResetToken.getCode());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.PASSWORD_RESET);
        verify(passwordResetConfirmationProcessor).process(passwordResetToken);
    }

    @Test
    @DisplayName("Should successfully process password reset")
    void testProcessPasswordReset_success() {
        String newHashedPassword = "newHashedPassword";
        String oldPasswordInTestUser = "oldHashedPassword";

        when(testUser.getPassword()).thenReturn(oldPasswordInTestUser);
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(anyString(), eq(oldPasswordInTestUser))).thenReturn(false);
        when(passwordEncoder.encode("newPassword")).thenReturn(newHashedPassword);

        verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword");

        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(passwordEncoder).matches("newPassword", oldPasswordInTestUser);
        verify(testUser).setPassword(newHashedPassword);
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(passwordResetToken);
    }

    @Test
    @DisplayName("Should throw SamePasswordException if new password is same as old")
    void testProcessPasswordReset_samePassword() {
        String oldPasswordInTestUser = "oldHashedPassword";
        when(testUser.getPassword()).thenReturn(oldPasswordInTestUser);

        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(anyString(), eq(oldPasswordInTestUser))).thenReturn(true);

        SamePasswordException exception = assertThrows(SamePasswordException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.SAME_PASSWORD);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(passwordEncoder).matches("newPassword", oldPasswordInTestUser);
        verifyNoMoreInteractions(userService, verificationTokenService);
    }

    @Test
    @DisplayName("Should throw InvalidVerificationPurposeException for wrong token purpose during password reset")
    void testProcessPasswordReset_invalidTokenPurpose() {
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        InvalidVerificationPurposeException exception = assertThrows(InvalidVerificationPurposeException.class,
                () -> verificationProcessingService.processPasswordReset(emailVerificationToken.getToken(), "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token not found during password reset")
    void testProcessPasswordReset_tokenNotFound() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset("nonexistent", "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException if token is expired during password reset")
    void testProcessPasswordReset_expiredToken() {
        passwordResetToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationTokenService).revokeToken(passwordResetToken);
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException if token is revoked during password reset")
    void testProcessPasswordReset_revokedToken() {
        passwordResetToken.setRevoked(true);
        when(verificationTokenService.findByToken(passwordResetToken.getToken()))
                .thenReturn(Optional.of(passwordResetToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetToken.getToken(), "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(passwordResetToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(passwordEncoder, userService);
    }
}
