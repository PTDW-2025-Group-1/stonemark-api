package services.auth;

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
import pt.estga.auth.services.passwordreset.PasswordResetServiceImpl;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.InvalidPasswordResetTokenException;
import pt.estga.shared.exceptions.UserNotFoundException;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServicePasswordResetTest {

    @Mock
    private VerificationTokenService verificationTokenService;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private SmsVerificationService smsVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PasswordResetServiceImpl passwordResetService;

    private User testUser;
    private UserContact testUserContact;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
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
    }

    @Test
    @DisplayName("Should initiate password reset for verified email")
    void testInitiatePasswordReset_email_success() {
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));
        when(verificationTokenService.createAndSaveToken(testUser, VerificationPurpose.PASSWORD_RESET))
                .thenReturn(new VerificationToken());

        passwordResetService.initiatePasswordReset(testEmail);

        verify(emailVerificationService).sendVerificationEmail(eq(testEmail), any(VerificationToken.class));
        verifyNoInteractions(smsVerificationService);
    }

    @Test
    @DisplayName("Should initiate password reset for verified telephone")
    void testInitiatePasswordReset_telephone_success() {
        String testTelephone = "123456789";
        testUserContact.setType(ContactType.TELEPHONE);
        testUserContact.setValue(testTelephone);
        when(userContactRepository.findByValue(testTelephone)).thenReturn(Optional.of(testUserContact));
        when(verificationTokenService.createAndSaveToken(testUser, VerificationPurpose.PASSWORD_RESET))
                .thenReturn(new VerificationToken());

        passwordResetService.initiatePasswordReset(testTelephone);

        verify(smsVerificationService).sendVerificationSms(eq(testTelephone), any(VerificationToken.class));
        verifyNoInteractions(emailVerificationService);
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if contact does not exist")
    void testInitiatePasswordReset_contactNotFound() {
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> passwordResetService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should throw ContactMethodNotAvailableException if contact is not verified")
    void testInitiatePasswordReset_contactNotVerified() {
        testUserContact.setVerified(false);
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));

        assertThrows(ContactMethodNotAvailableException.class, () -> passwordResetService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should throw UserNotFoundException if user is disabled")
    void testInitiatePasswordReset_userDisabled() {
        testUser.setEnabled(false);
        when(userContactRepository.findByValue(testEmail)).thenReturn(Optional.of(testUserContact));

        assertThrows(UserNotFoundException.class, () -> passwordResetService.initiatePasswordReset(testEmail));
    }

    @Test
    @DisplayName("Should reset password with a valid token")
    void testResetPassword_success() {
        String token = UUID.randomUUID().toString();
        String newPassword = "newPassword";
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(testUser)
                .purpose(VerificationPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isRevoked(false)
                .build();

        when(verificationTokenService.findByToken(token)).thenReturn(Optional.of(verificationToken));
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        passwordResetService.resetPassword(token, newPassword);

        verify(passwordEncoder).encode(newPassword);
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(verificationToken);
        assertThat(testUser.getPassword()).isEqualTo("encodedPassword");
    }

    @Test
    @DisplayName("Should throw InvalidPasswordResetTokenException for invalid token")
    void testResetPassword_invalidToken() {
        String token = "invalidToken";
        when(verificationTokenService.findByToken(token)).thenReturn(Optional.empty());

        assertThrows(InvalidPasswordResetTokenException.class, () -> passwordResetService.resetPassword(token, "newPassword"));
    }

    @Test
    @DisplayName("Should validate a valid password reset token")
    void testValidatePasswordResetToken_valid() {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(testUser)
                .purpose(VerificationPurpose.PASSWORD_RESET)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(verificationTokenService.findByToken(token)).thenReturn(Optional.of(verificationToken));

        Optional<User> result = passwordResetService.validatePasswordResetToken(token);

        assertThat(result).isPresent().contains(testUser);
    }

    @Test
    @DisplayName("Should not validate an invalid password reset token")
    void testValidatePasswordResetToken_invalid() {
        String token = "invalidToken";
        when(verificationTokenService.findByToken(token)).thenReturn(Optional.empty());

        Optional<User> result = passwordResetService.validatePasswordResetToken(token);

        assertThat(result).isNotPresent();
    }
}
