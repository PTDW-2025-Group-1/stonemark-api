package services.verification;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.auth.services.verification.processing.TelephoneVerificationProcessor;
import pt.estga.auth.services.verification.processing.VerificationProcessorFactory;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.VerificationErrorMessages;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceImplTelephoneTest {

    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private VerificationProcessorFactory verificationProcessorFactory;
    @Mock
    private TelephoneVerificationProcessor telephoneVerificationProcessor;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    @Mock
    private User testUser;
    private VerificationToken telephoneVerificationToken;

    @BeforeEach
    void setUp() {
        telephoneVerificationToken = VerificationToken.builder()
                .token("uuid-telephone-verify")
                .code("789012")
                .user(testUser)
                .purpose(VerificationPurpose.TELEPHONE_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();
    }

    @Test
    @DisplayName("Should confirm telephone verification token successfully")
    void testConfirmToken_telephoneVerification_success() {
        when(verificationTokenService.findByToken(telephoneVerificationToken.getToken()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationPurpose.TELEPHONE_VERIFICATION))
                .thenReturn(telephoneVerificationProcessor);
        when(telephoneVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmToken(telephoneVerificationToken.getToken());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByToken(telephoneVerificationToken.getToken());
        verify(verificationProcessorFactory).getProcessor(VerificationPurpose.TELEPHONE_VERIFICATION);
        verify(telephoneVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should confirm telephone verification code successfully")
    void testConfirmCode_telephoneVerification_success() {
        when(verificationTokenService.findByCode(telephoneVerificationToken.getCode()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(verificationProcessorFactory.getProcessor(VerificationPurpose.TELEPHONE_VERIFICATION))
                .thenReturn(telephoneVerificationProcessor);
        when(telephoneVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(telephoneVerificationToken.getCode());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByCode(telephoneVerificationToken.getCode());
        verify(verificationProcessorFactory).getProcessor(VerificationPurpose.TELEPHONE_VERIFICATION);
        verify(telephoneVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for invalid token confirmation")
    void testConfirmToken_invalidToken() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.confirmToken("nonexistent"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired token confirmation")
    void testConfirmToken_expiredToken() {
        telephoneVerificationToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(telephoneVerificationToken.getToken()))
                .thenReturn(Optional.of(telephoneVerificationToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.confirmToken(telephoneVerificationToken.getToken()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(telephoneVerificationToken.getToken());
        verify(verificationTokenService).revokeToken(telephoneVerificationToken);
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException for revoked token confirmation")
    void testConfirmToken_revokedToken() {
        telephoneVerificationToken.setRevoked(true);
        when(verificationTokenService.findByToken(telephoneVerificationToken.getToken()))
                .thenReturn(Optional.of(telephoneVerificationToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.confirmToken(telephoneVerificationToken.getToken()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(telephoneVerificationToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for invalid code confirmation")
    void testConfirmCode_invalidCode() {
        when(verificationTokenService.findByCode(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.confirmCode("nonexistent"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_NOT_FOUND);
        verify(verificationTokenService).findByCode("nonexistent");
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired code confirmation")
    void testConfirmCode_expiredCode() {
        telephoneVerificationToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByCode(telephoneVerificationToken.getCode()))
                .thenReturn(Optional.of(telephoneVerificationToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.confirmCode(telephoneVerificationToken.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_EXPIRED);
        verify(verificationTokenService).findByCode(telephoneVerificationToken.getCode());
        verify(verificationTokenService).revokeToken(telephoneVerificationToken);
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException for revoked code confirmation")
    void testConfirmCode_revokedCode() {
        telephoneVerificationToken.setRevoked(true);
        when(verificationTokenService.findByCode(telephoneVerificationToken.getCode()))
                .thenReturn(Optional.of(telephoneVerificationToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.confirmCode(telephoneVerificationToken.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_REVOKED);
        verify(verificationTokenService).findByCode(telephoneVerificationToken.getCode());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(verificationProcessorFactory, telephoneVerificationProcessor);
    }
}
