package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.verification.ActionCodeValidationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.auth.services.verification.processors.VerificationPurposeProcessor;
import pt.estga.shared.exceptions.InvalidVerificationPurposeException;
import pt.estga.shared.exceptions.SamePasswordException;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceTest {

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
    private VerificationPurposeProcessor resetPasswordProcessor;

    private VerificationProcessingService verificationProcessingService;

    @BeforeEach
    void setUp() {
        VerificationProcessingServiceImpl serviceImpl = new VerificationProcessingServiceImpl(
                actionCodeService,
                userService,
                passwordEncoder,
                actionCodeValidationService,
                List.of(emailVerificationProcessor, resetPasswordProcessor)
        );
        when(emailVerificationProcessor.getType()).thenReturn(ActionCodeType.EMAIL_VERIFICATION);
        when(resetPasswordProcessor.getType()).thenReturn(ActionCodeType.RESET_PASSWORD);
        serviceImpl.init();
        verificationProcessingService = serviceImpl;
    }

    @Test
    void confirmCode_shouldProcessEmailVerification_whenCodeIsValid() {
        String code = "validCode";
        ActionCode actionCode = new ActionCode();
        actionCode.setType(ActionCodeType.EMAIL_VERIFICATION);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);
        when(emailVerificationProcessor.process(actionCode)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(code);

        assertTrue(result.isEmpty());
        verify(emailVerificationProcessor).process(actionCode);
    }

    @Test
    void confirmCode_shouldProcessPasswordReset_whenCodeIsValid() {
        String code = "validCode";
        ActionCode actionCode = new ActionCode();
        actionCode.setType(ActionCodeType.RESET_PASSWORD);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);
        when(resetPasswordProcessor.process(actionCode)).thenReturn(Optional.of(code));

        Optional<String> result = verificationProcessingService.confirmCode(code);

        assertTrue(result.isPresent());
        assertEquals(code, result.get());
        verify(resetPasswordProcessor).process(actionCode);
    }

    @Test
    void confirmCode_shouldThrowException_whenCodeTypeIsInvalid() {
        String code = "validCode";
        ActionCode actionCode = new ActionCode();
        actionCode.setType(ActionCodeType.DEVICE_VERIFICATION);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);

        assertThrows(InvalidVerificationPurposeException.class, () -> verificationProcessingService.confirmCode(code));
    }

    @Test
    void processPasswordReset_shouldResetPassword_whenCodeIsValid() {
        String code = "validCode";
        String newPassword = "newPassword";
        User user = new User();
        user.setPassword("oldPassword");
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.RESET_PASSWORD);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);
        when(passwordEncoder.matches(newPassword, "oldPassword")).thenReturn(false);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");

        verificationProcessingService.processPasswordReset(code, newPassword);

        verify(userService).update(user);
        assertEquals("encodedPassword", user.getPassword());
        verify(actionCodeService).consumeCode(actionCode);
    }

    @Test
    void processPasswordReset_shouldThrowException_whenCodeTypeIsInvalid() {
        String code = "validCode";
        String newPassword = "newPassword";
        ActionCode actionCode = new ActionCode();
        actionCode.setType(ActionCodeType.EMAIL_VERIFICATION);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);

        assertThrows(InvalidVerificationPurposeException.class, () -> verificationProcessingService.processPasswordReset(code, newPassword));
    }

    @Test
    void processPasswordReset_shouldThrowException_whenPasswordIsTheSame() {
        String code = "validCode";
        String newPassword = "newPassword";
        User user = new User();
        user.setPassword("encodedPassword");
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.RESET_PASSWORD);

        when(actionCodeValidationService.getValidatedActionCode(code)).thenReturn(actionCode);
        when(passwordEncoder.matches(newPassword, "encodedPassword")).thenReturn(true);

        assertThrows(SamePasswordException.class, () -> verificationProcessingService.processPasswordReset(code, newPassword));
    }

    @Test
    void validatePasswordResetToken_shouldReturnUser_whenTokenIsValid() {
        String code = "validCode";
        User user = new User();
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.RESET_PASSWORD);
        actionCode.setConsumed(false);
        actionCode.setExpiresAt(Instant.now().plusSeconds(60));

        when(actionCodeService.findByCode(code)).thenReturn(Optional.of(actionCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(code);

        assertTrue(result.isPresent());
        assertEquals(user, result.get());
    }

    @Test
    void validatePasswordResetToken_shouldReturnEmpty_whenTokenIsInvalid() {
        String code = "invalidCode";

        when(actionCodeService.findByCode(code)).thenReturn(Optional.empty());

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(code);

        assertTrue(result.isEmpty());
    }

    @Test
    void validatePasswordResetToken_shouldReturnEmpty_whenTokenIsConsumed() {
        String code = "validCode";
        User user = new User();
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.RESET_PASSWORD);
        actionCode.setConsumed(true);
        actionCode.setExpiresAt(Instant.now().plusSeconds(60));

        when(actionCodeService.findByCode(code)).thenReturn(Optional.of(actionCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(code);

        assertTrue(result.isEmpty());
    }

    @Test
    void validatePasswordResetToken_shouldReturnEmpty_whenTokenIsExpired() {
        String code = "validCode";
        User user = new User();
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.RESET_PASSWORD);
        actionCode.setConsumed(false);
        actionCode.setExpiresAt(Instant.now().minusSeconds(60));

        when(actionCodeService.findByCode(code)).thenReturn(Optional.of(actionCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(code);

        assertTrue(result.isEmpty());
    }

    @Test
    void validatePasswordResetToken_shouldReturnEmpty_whenTokenTypeIsInvalid() {
        String code = "validCode";
        User user = new User();
        ActionCode actionCode = new ActionCode();
        actionCode.setUser(user);
        actionCode.setType(ActionCodeType.EMAIL_VERIFICATION);
        actionCode.setConsumed(false);
        actionCode.setExpiresAt(Instant.now().plusSeconds(60));

        when(actionCodeService.findByCode(code)).thenReturn(Optional.of(actionCode));

        Optional<User> result = verificationProcessingService.validatePasswordResetToken(code);

        assertTrue(result.isEmpty());
    }
}
