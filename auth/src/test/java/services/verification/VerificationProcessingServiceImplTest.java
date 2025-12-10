package services.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.verification.ActionCodeValidationService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.auth.services.verification.processors.VerificationPurposeProcessor;
import pt.estga.shared.exceptions.*;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceImplTest {

    @Mock
    private ActionCodeService actionCodeService;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ActionCodeValidationService actionCodeValidationService;
    @Mock
    private VerificationPurposeProcessor emailVerificationProcessor;
    @Mock
    private VerificationPurposeProcessor telephoneVerificationProcessor;
    @Mock
    private VerificationPurposeProcessor passwordResetProcessor;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    private User testUser;
    private ActionCode emailVerificationCode;
    private ActionCode telephoneVerificationCode;
    private ActionCode passwordResetCode;


    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("oldPassword")
                .enabled(false)
                .build();

        emailVerificationCode = ActionCode.builder()
                .code("ABCDEF")
                .user(testUser)
                .type(ActionCodeType.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .consumed(false)
                .build();

        telephoneVerificationCode = ActionCode.builder()
                .code("789012")
                .user(testUser)
                .type(ActionCodeType.PHONE_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .consumed(false)
                .build();

        passwordResetCode = ActionCode.builder()
                .code("GHIJKL")
                .user(testUser)
                .type(ActionCodeType.RESET_PASSWORD)
                .expiresAt(Instant.now().plusSeconds(3600))
                .consumed(false)
                .build();

        // Configure mock processors
        when(emailVerificationProcessor.getType()).thenReturn(ActionCodeType.EMAIL_VERIFICATION);
        when(telephoneVerificationProcessor.getType()).thenReturn(ActionCodeType.PHONE_VERIFICATION);
        when(passwordResetProcessor.getType()).thenReturn(ActionCodeType.RESET_PASSWORD);

        // Manually inject the list of processors into the service and call init()
        List<VerificationPurposeProcessor> purposeProcessors = Arrays.asList(
                emailVerificationProcessor,
                telephoneVerificationProcessor,
                passwordResetProcessor
        );
        verificationProcessingService = new VerificationProcessingServiceImpl(
                actionCodeService,
                userService,
                passwordEncoder,
                actionCodeValidationService,
                purposeProcessors
        );
        verificationProcessingService.init();

        // Clear invocations on mocks after init() to reset their state for each test
        clearInvocations(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should confirm email verification code successfully")
    void testConfirmCode_emailVerification_success() {
        when(actionCodeValidationService.getValidatedActionCode(emailVerificationCode.getCode()))
                .thenReturn(emailVerificationCode);
        when(emailVerificationProcessor.process(emailVerificationCode)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(emailVerificationCode.getCode());

        assertThat(result).isEmpty();
        verify(actionCodeValidationService).getValidatedActionCode(emailVerificationCode.getCode());
        verify(emailVerificationProcessor).process(emailVerificationCode);
        verifyNoInteractions(telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should confirm telephone verification code successfully")
    void testConfirmCode_telephoneVerification_success() {
        when(actionCodeValidationService.getValidatedActionCode(telephoneVerificationCode.getCode()))
                .thenReturn(telephoneVerificationCode);
        when(telephoneVerificationProcessor.process(telephoneVerificationCode)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(telephoneVerificationCode.getCode());

        assertThat(result).isEmpty();
        verify(actionCodeValidationService).getValidatedActionCode(telephoneVerificationCode.getCode());
        verify(telephoneVerificationProcessor).process(telephoneVerificationCode);
        verifyNoInteractions(emailVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should return code for password reset purpose")
    void testConfirmCode_passwordReset_success() {
        when(actionCodeValidationService.getValidatedActionCode(passwordResetCode.getCode()))
                .thenReturn(passwordResetCode);
        when(passwordResetProcessor.process(passwordResetCode)).thenReturn(Optional.of(passwordResetCode.getCode()));

        Optional<String> result = verificationProcessingService.confirmCode(passwordResetCode.getCode());

        assertThat(result).isPresent().contains(passwordResetCode.getCode());
        verify(actionCodeValidationService).getValidatedActionCode(passwordResetCode.getCode());
        verify(passwordResetProcessor).process(passwordResetCode);
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for invalid code confirmation")
    void testConfirmCode_invalidCode() {
        when(actionCodeValidationService.getValidatedActionCode(anyString()))
                .thenThrow(new InvalidTokenException(VerificationErrorMessages.CODE_NOT_FOUND));

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.confirmCode("nonexistent"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_NOT_FOUND);
        verify(actionCodeValidationService).getValidatedActionCode("nonexistent");
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired code confirmation")
    void testConfirmCode_expiredCode() {
        when(actionCodeValidationService.getValidatedActionCode(anyString()))
                .thenThrow(new TokenExpiredException(VerificationErrorMessages.CODE_EXPIRED));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.confirmCode(emailVerificationCode.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_EXPIRED);
        verify(actionCodeValidationService).getValidatedActionCode(emailVerificationCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException for consumed code confirmation")
    void testConfirmCode_consumedCode() {
        when(actionCodeValidationService.getValidatedActionCode(anyString()))
                .thenThrow(new TokenRevokedException(VerificationErrorMessages.CODE_REVOKED));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.confirmCode(emailVerificationCode.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_REVOKED);
        verify(actionCodeValidationService).getValidatedActionCode(emailVerificationCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should process password reset successfully")
    void testProcessPasswordReset_success() {
        String newPassword = "newPassword123";
        String oldHashedPassword = testUser.getPassword();

        when(actionCodeValidationService.getValidatedActionCode(passwordResetCode.getCode()))
                .thenReturn(passwordResetCode);
        when(passwordEncoder.matches(newPassword, oldHashedPassword)).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedNewPassword");

        verificationProcessingService.processPasswordReset(passwordResetCode.getCode(), newPassword);

        verify(actionCodeValidationService).getValidatedActionCode(passwordResetCode.getCode());
        verify(passwordEncoder).matches(newPassword, oldHashedPassword);
        verify(passwordEncoder).encode(newPassword);
        verify(userService).update(testUser);
        verify(actionCodeService).consumeCode(passwordResetCode);
        assertThat(testUser.getPassword()).isEqualTo("encodedNewPassword");
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should throw InvalidVerificationPurposeException if code purpose is not RESET_PASSWORD")
    void testProcessPasswordReset_invalidPurpose() {
        when(actionCodeValidationService.getValidatedActionCode(emailVerificationCode.getCode()))
                .thenReturn(emailVerificationCode); // Use email code which is not RESET_PASSWORD

        InvalidVerificationPurposeException exception = assertThrows(InvalidVerificationPurposeException.class,
                () -> verificationProcessingService.processPasswordReset(emailVerificationCode.getCode(), "newPassword"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        verify(actionCodeValidationService).getValidatedActionCode(emailVerificationCode.getCode());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(userService);
        verifyNoInteractions(actionCodeService);
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should throw SamePasswordException if new password is the same as old password")
    void testProcessPasswordReset_samePassword() {
        String samePassword = "oldPassword";
        when(actionCodeValidationService.getValidatedActionCode(passwordResetCode.getCode()))
                .thenReturn(passwordResetCode);
        when(passwordEncoder.matches(samePassword, testUser.getPassword())).thenReturn(true);

        SamePasswordException exception = assertThrows(SamePasswordException.class,
                () -> verificationProcessingService.processPasswordReset(passwordResetCode.getCode(), samePassword));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.SAME_PASSWORD);
        verify(actionCodeValidationService).getValidatedActionCode(passwordResetCode.getCode());
        verify(passwordEncoder).matches(samePassword, testUser.getPassword());
        verifyNoMoreInteractions(passwordEncoder);
        verifyNoInteractions(userService);
        verifyNoInteractions(actionCodeService);
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should validate password reset code successfully")
    void testValidatePasswordResetCode_success() {
        when(actionCodeService.findByCode(passwordResetCode.getCode()))
                .thenReturn(Optional.of(passwordResetCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(passwordResetCode.getCode());

        assertThat(result).isPresent().contains(testUser);
        verify(actionCodeService).findByCode(passwordResetCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should return empty for invalid password reset code")
    void testValidatePasswordResetCode_invalidCode() {
        when(actionCodeService.findByCode(anyString())).thenReturn(Optional.empty());

        Optional<User> result = verificationProcessingService.validatePasswordResetToken("invalid");

        assertThat(result).isEmpty();
        verify(actionCodeService).findByCode("invalid");
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should return empty for consumed password reset code")
    void testValidatePasswordResetCode_consumedCode() {
        passwordResetCode.setConsumed(true);
        when(actionCodeService.findByCode(passwordResetCode.getCode()))
                .thenReturn(Optional.of(passwordResetCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(passwordResetCode.getCode());

        assertThat(result).isEmpty();
        verify(actionCodeService).findByCode(passwordResetCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should return empty for expired password reset code")
    void testValidatePasswordResetCode_expiredCode() {
        passwordResetCode.setExpiresAt(Instant.now().minusSeconds(10));
        when(actionCodeService.findByCode(passwordResetCode.getCode()))
                .thenReturn(Optional.of(passwordResetCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(passwordResetCode.getCode());

        assertThat(result).isEmpty();
        verify(actionCodeService).findByCode(passwordResetCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }

    @Test
    @DisplayName("Should return empty for password reset code with wrong purpose")
    void testValidatePasswordResetCode_wrongPurpose() {
        when(actionCodeService.findByCode(emailVerificationCode.getCode()))
                .thenReturn(Optional.of(emailVerificationCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(emailVerificationCode.getCode());

        assertThat(result).isEmpty();
        verify(actionCodeService).findByCode(emailVerificationCode.getCode());
        verifyNoInteractions(emailVerificationProcessor, telephoneVerificationProcessor, passwordResetProcessor);
    }
}
