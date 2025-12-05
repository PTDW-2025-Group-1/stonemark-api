package services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.auth.services.verification.processing.TelephoneChangeConfirmProcessor;
import pt.estga.auth.services.verification.processing.TelephoneChangeRequestProcessor;
import pt.estga.auth.services.verification.processing.TelephoneVerificationProcessor;
import pt.estga.auth.services.verification.processing.VerificationProcessorFactory;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.SameTelephoneException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VerificationProcessingServiceImplTelephoneTest {

    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private VerificationProcessorFactory verificationProcessorFactory;
    @Mock
    private UserService userService;
    @Mock
    private TelephoneVerificationProcessor telephoneVerificationProcessor;
    @Mock
    private TelephoneChangeRequestProcessor telephoneChangeRequestProcessor;
    @Mock
    private TelephoneChangeConfirmProcessor telephoneChangeConfirmProcessor;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    @Mock
    private User testUser;
    private VerificationToken telephoneVerificationToken;
    private VerificationToken telephoneChangeRequestToken;
    private VerificationToken telephoneChangeConfirmToken;

    @BeforeEach
    void setUp() {
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getEmail()).thenReturn("test@example.com");
        when(testUser.getPassword()).thenReturn("oldHashedPassword");
        when(testUser.isEnabled()).thenReturn(false);
        when(testUser.getTelephone()).thenReturn("123456789");

        telephoneVerificationToken = VerificationToken.builder()
                .token("uuid-telephone-verify")
                .code("789012")
                .user(testUser)
                .purpose(VerificationTokenPurpose.TELEPHONE_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        telephoneChangeRequestToken = VerificationToken.builder()
                .token("uuid-telephone-change-request")
                .code("987654")
                .user(testUser)
                .purpose(VerificationTokenPurpose.TELEPHONE_CHANGE_REQUEST)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        telephoneChangeConfirmToken = VerificationToken.builder()
                .token("uuid-telephone-change-confirm")
                .code("initialPlaceholderCode")
                .user(testUser)
                .purpose(VerificationTokenPurpose.TELEPHONE_CHANGE_CONFIRM)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
    }

    @Test
    @DisplayName("Should confirm telephone verification token successfully")
    void testConfirmToken_telephoneVerification_success() {
        when(verificationTokenService.findByToken(telephoneVerificationToken.getToken()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.TELEPHONE_VERIFICATION))
                .thenReturn(telephoneVerificationProcessor);
        when(telephoneVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmToken(telephoneVerificationToken.getToken());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByToken(telephoneVerificationToken.getToken());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.TELEPHONE_VERIFICATION);
        verify(telephoneVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should confirm telephone verification code successfully")
    void testConfirmCode_telephoneVerification_success() {
        when(verificationTokenService.findByCode(telephoneVerificationToken.getCode()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationTokenPurpose.TELEPHONE_VERIFICATION))
                .thenReturn(telephoneVerificationProcessor);
        when(telephoneVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(telephoneVerificationToken.getCode());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByCode(telephoneVerificationToken.getCode());
        verify(verificationProcessorFactory).getProcessor(VerificationTokenPurpose.TELEPHONE_VERIFICATION);
        verify(telephoneVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should successfully process telephone change")
    void testProcessTelephoneChange_success() {
        String newTelephone = "987654321";
        when(testUser.getTelephone()).thenReturn("123456789");
        when(verificationTokenService.findByToken(telephoneChangeRequestToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeRequestToken));

        verificationProcessingService.processTelephoneChange(telephoneChangeRequestToken.getToken(), newTelephone);

        verify(verificationTokenService).findByToken(telephoneChangeRequestToken.getToken());
        verify(testUser).setTelephone(newTelephone);
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(telephoneChangeRequestToken);
    }

    @Test
    @DisplayName("Should throw SameTelephoneException if new telephone is same as old")
    void testProcessTelephoneChange_sameTelephone() {
        String newTelephone = "123456789";
        when(testUser.getTelephone()).thenReturn("123456789");
        when(verificationTokenService.findByToken(telephoneChangeRequestToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeRequestToken));

        SameTelephoneException exception = assertThrows(SameTelephoneException.class,
                () -> verificationProcessingService.processTelephoneChange(telephoneChangeRequestToken.getToken(), newTelephone));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.SAME_TELEPHONE);
        verify(verificationTokenService).findByToken(telephoneChangeRequestToken.getToken());
        verify(testUser, never()).setTelephone(anyString());
        verifyNoMoreInteractions(userService, verificationTokenService);
    }

    @Test
    @DisplayName("Should throw InvalidVerificationPurposeException for wrong token purpose during telephone change")
    void testProcessTelephoneChange_invalidTokenPurpose() {
        VerificationToken emailVerificationToken = VerificationToken.builder()
                .token("uuid-email-verify")
                .code("ABCDEF")
                .user(testUser)
                .purpose(VerificationTokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        InvalidVerificationPurposeException exception = assertThrows(InvalidVerificationPurposeException.class,
                () -> verificationProcessingService.processTelephoneChange(emailVerificationToken.getToken(), "newTelephone"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_TELEPHONE_CHANGE);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token not found during telephone change")
    void testProcessTelephoneChange_tokenNotFound() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processTelephoneChange("nonexistent", "newTelephone"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException if token is expired during telephone change")
    void testProcessTelephoneChange_expiredToken() {
        telephoneChangeRequestToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(telephoneChangeRequestToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeRequestToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.processTelephoneChange(telephoneChangeRequestToken.getToken(), "newTelephone"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(telephoneChangeRequestToken.getToken());
        verify(verificationTokenService).revokeToken(telephoneChangeRequestToken);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException if token is revoked during telephone change")
    void testProcessTelephoneChange_revokedToken() {
        telephoneChangeRequestToken.setRevoked(true);
        when(verificationTokenService.findByToken(telephoneChangeRequestToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeRequestToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.processTelephoneChange(telephoneChangeRequestToken.getToken(), "newTelephone"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(telephoneChangeRequestToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should successfully process telephone change confirmation")
    void testProcessTelephoneChangeConfirm_success() {
        String newTelephone = "987654321";
        when(testUser.getTelephone()).thenReturn("123456789");

        telephoneChangeConfirmToken.setCode(newTelephone);

        when(verificationTokenService.findByToken(telephoneChangeConfirmToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeConfirmToken));

        verificationProcessingService.processTelephoneChangeConfirm(telephoneChangeConfirmToken.getToken(), newTelephone);

        verify(verificationTokenService).findByToken(telephoneChangeConfirmToken.getToken());
        verify(testUser).setTelephone(newTelephone);
        verify(userService).update(testUser);
        verify(verificationTokenService).revokeToken(telephoneChangeConfirmToken);
    }

    @Test
    @DisplayName("Should throw InvalidVerificationPurposeException for wrong token purpose during telephone change confirmation")
    void testProcessTelephoneChangeConfirm_invalidTokenPurpose() {
        VerificationToken emailVerificationToken = VerificationToken.builder()
                .token("uuid-email-verify")
                .code("ABCDEF")
                .user(testUser)
                .purpose(VerificationTokenPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        InvalidVerificationPurposeException exception = assertThrows(InvalidVerificationPurposeException.class,
                () -> verificationProcessingService.processTelephoneChangeConfirm(emailVerificationToken.getToken(), "someCode"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_TELEPHONE_CHANGE_CONFIRM);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException if token not found during telephone change confirmation")
    void testProcessTelephoneChangeConfirm_tokenNotFound() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.processTelephoneChangeConfirm("nonexistent", "someCode"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException if token is expired during telephone change confirmation")
    void testProcessTelephoneChangeConfirm_expiredToken() {
        telephoneChangeConfirmToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(telephoneChangeConfirmToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeConfirmToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.processTelephoneChangeConfirm(telephoneChangeConfirmToken.getToken(), "someCode"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(telephoneChangeConfirmToken.getToken());
        verify(verificationTokenService).revokeToken(telephoneChangeConfirmToken);
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException if token is revoked during telephone change confirmation")
    void testProcessTelephoneChangeConfirm_revokedToken() {
        telephoneChangeConfirmToken.setRevoked(true);
        when(verificationTokenService.findByToken(telephoneChangeConfirmToken.getToken()))
                .thenReturn(Optional.of(telephoneChangeConfirmToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.processTelephoneChangeConfirm(telephoneChangeConfirmToken.getToken(), "someCode"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(telephoneChangeConfirmToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(userService);
    }
}
