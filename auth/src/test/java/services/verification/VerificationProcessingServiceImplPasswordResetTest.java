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
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.shared.exceptions.*;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceImplPasswordResetTest {

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private VerificationDispatchService verificationDispatchService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    private User testUser;
    private UserContact testUserContact;
    private VerificationToken passwordResetToken;
    private final String testEmail = "test@example.com";
    private final String tokenValue = UUID.randomUUID().toString();

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("oldHashedPassword")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .build();

        testUserContact = UserContact.builder()
                .id(1L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .isPrimary(true)
                .isVerified(true)
                .user(testUser)
                .build();

        passwordResetToken = VerificationToken.builder()
                .token(tokenValue)
                .user(testUser)
                .purpose(VerificationPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isRevoked(false)
                .build();
    }

    @Test
    @DisplayName("Should initiate password reset for verified contact")
    void testInitiatePasswordReset_success() {
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));
        when(verificationTokenService.createAndSaveToken(testUser, VerificationPurpose.PASSWORD_RESET))
                .thenReturn(new VerificationToken());

        verificationProcessingService.initiatePasswordReset(testEmail);

        verify(verificationDispatchService).sendVerification(eq(testUserContact), any(VerificationToken.class));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if contact does not exist")
    void testInitiatePasswordReset_contactNotFound() {
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> verificationProcessingService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should throw ContactMethodNotAvailableException if contact is not verified")
    void testInitiatePasswordReset_contactNotVerified() {
        testUserContact.setVerified(false);
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));

        assertThrows(ContactMethodNotAvailableException.class, () -> verificationProcessingService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user is disabled")
    void testInitiatePasswordReset_userDisabled() {
        testUser.setEnabled(false);
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));

        assertThrows(UserNotFoundException.class, () -> verificationProcessingService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should reset password with a valid token")
    void testProcessPasswordReset_success() {
        String newPassword = "newPassword";
        String newHashedPassword = "newHashedPassword";
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(newPassword, testUser.getPassword())).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn(newHashedPassword);

        verificationProcessingService.processPasswordReset(tokenValue, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(passwordResetToken);
        assertThat(testUser.getPassword()).isEqualTo(newHashedPassword);
    }

    @Test
    @DisplayName("Should throw SamePasswordException if new password is same as old")
    void testProcessPasswordReset_samePassword() {
        String samePassword = "oldPassword";
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));
        when(passwordEncoder.matches(samePassword, testUser.getPassword())).thenReturn(true);

        SamePasswordException exception = assertThrows(SamePasswordException.class,
                () -> verificationProcessingService.processPasswordReset(tokenValue, samePassword));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.SAME_PASSWORD);
        verifyNoMoreInteractions(userService, verificationTokenService);
    }

    @Test
    @DisplayName("Should throw InvalidVerificationPurposeException for wrong token purpose")
    void testProcessPasswordReset_invalidTokenPurpose() {
        passwordResetToken.setPurpose(VerificationPurpose.EMAIL_VERIFICATION);
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));

        InvalidVerificationPurposeException exception = assertThrows(InvalidVerificationPurposeException.class,
                () -> verificationProcessingService.processPasswordReset(tokenValue, "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        verifyNoInteractions(passwordEncoder, userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token not found")
    void testProcessPasswordReset_tokenNotFound() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processPasswordReset("nonexistent", "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException if token is expired")
    void testProcessPasswordReset_expiredToken() {
        passwordResetToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.processPasswordReset(tokenValue, "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).revokeToken(passwordResetToken);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException if token is already revoked")
    void testProcessPasswordReset_revokedToken() {
        passwordResetToken.setRevoked(true);
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.processPasswordReset(tokenValue, "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService, never()).revokeToken(any());
    }

    @Test
    @DisplayName("Should validate a valid password reset token")
    void testValidatePasswordResetToken_valid() {
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.of(passwordResetToken));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(tokenValue);

        assertThat(result).isPresent().contains(testUser);
    }

    @Test
    @DisplayName("Should not validate an invalid password reset token")
    void testValidatePasswordResetToken_invalid() {
        when(verificationTokenService.findByToken(tokenValue)).thenReturn(Optional.empty());

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(tokenValue);

        assertThat(result).isNotPresent();
    }
}
