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
import pt.estga.auth.services.verification.GenericVerificationProcessor;
import pt.estga.auth.services.verification.VerificationProcessingServiceImpl;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.VerificationErrorMessages;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessingServiceImplTest {

    @Mock
    private VerificationTokenService verificationTokenService;
    @Mock
    private GenericVerificationProcessor genericVerificationProcessor;
    @Mock
    private UserService userService;

    @InjectMocks
    private VerificationProcessingServiceImpl verificationProcessingService;

    private User testUser;
    private VerificationToken emailVerificationToken;
    private VerificationToken telephoneVerificationToken;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .enabled(false)
                .build();

        emailVerificationToken = VerificationToken.builder()
                .token("uuid-email-verify")
                .code("ABCDEF")
                .user(testUser)
                .purpose(VerificationPurpose.EMAIL_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isRevoked(false)
                .build();

        telephoneVerificationToken = VerificationToken.builder()
                .token("uuid-telephone-verify")
                .code("789012")
                .user(testUser)
                .purpose(VerificationPurpose.TELEPHONE_VERIFICATION)
                .expiresAt(Instant.now().plusSeconds(3600))
                .isRevoked(false)
                .build();
    }

    @Test
    @DisplayName("Should confirm email verification token successfully")
    void testConfirmToken_emailVerification_success() {
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));
        when(genericVerificationProcessor.process(emailVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmToken(emailVerificationToken.getToken());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(genericVerificationProcessor).process(emailVerificationToken);
    }

    @Test
    @DisplayName("Should confirm telephone verification token successfully")
    void testConfirmToken_telephoneVerification_success() {
        when(verificationTokenService.findByToken(telephoneVerificationToken.getToken()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(genericVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmToken(telephoneVerificationToken.getToken());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByToken(telephoneVerificationToken.getToken());
        verify(genericVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for invalid token confirmation")
    void testConfirmToken_invalidToken() {
        when(verificationTokenService.findByToken(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.confirmToken("nonexistent"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_NOT_FOUND);
        verify(verificationTokenService).findByToken("nonexistent");
        verifyNoInteractions(genericVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired token confirmation")
    void testConfirmToken_expiredToken() {
        emailVerificationToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.confirmToken(emailVerificationToken.getToken()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_EXPIRED);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(verificationTokenService).revokeToken(emailVerificationToken);
        verifyNoInteractions(genericVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException for revoked token confirmation")
    void testConfirmToken_revokedToken() {
        emailVerificationToken.setRevoked(true);
        when(verificationTokenService.findByToken(emailVerificationToken.getToken()))
                .thenReturn(Optional.of(emailVerificationToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.confirmToken(emailVerificationToken.getToken()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.TOKEN_REVOKED);
        verify(verificationTokenService).findByToken(emailVerificationToken.getToken());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(genericVerificationProcessor);
    }

    @Test
    @DisplayName("Should confirm email verification code successfully")
    void testConfirmCode_emailVerification_success() {
        when(verificationTokenService.findByCode(emailVerificationToken.getCode()))
                .thenReturn(Optional.of(emailVerificationToken));
        when(genericVerificationProcessor.process(emailVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(emailVerificationToken.getCode());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByCode(emailVerificationToken.getCode());
        verify(genericVerificationProcessor).process(emailVerificationToken);
    }

    @Test
    @DisplayName("Should confirm telephone verification code successfully")
    void testConfirmCode_telephoneVerification_success() {
        when(verificationTokenService.findByCode(telephoneVerificationToken.getCode()))
                .thenReturn(Optional.of(telephoneVerificationToken));
        when(genericVerificationProcessor.process(telephoneVerificationToken)).thenReturn(Optional.empty());

        Optional<String> result = verificationProcessingService.confirmCode(telephoneVerificationToken.getCode());

        assertThat(result).isEmpty();
        verify(verificationTokenService).findByCode(telephoneVerificationToken.getCode());
        verify(genericVerificationProcessor).process(telephoneVerificationToken);
    }

    @Test
    @DisplayName("Should throw InvalidTokenException for invalid code confirmation")
    void testConfirmCode_invalidCode() {
        when(verificationTokenService.findByCode(anyString())).thenReturn(Optional.empty());

        InvalidTokenException exception = assertThrows(InvalidTokenException.class,
                () -> verificationProcessingService.confirmCode("nonexistent"));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_NOT_FOUND);
        verify(verificationTokenService).findByCode("nonexistent");
        verifyNoInteractions(genericVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenExpiredException for expired code confirmation")
    void testConfirmCode_expiredCode() {
        emailVerificationToken.setExpiresAt(Instant.now().minusSeconds(10));
        when(verificationTokenService.findByCode(emailVerificationToken.getCode()))
                .thenReturn(Optional.of(emailVerificationToken));

        TokenExpiredException exception = assertThrows(TokenExpiredException.class,
                () -> verificationProcessingService.confirmCode(emailVerificationToken.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_EXPIRED);
        verify(verificationTokenService).findByCode(emailVerificationToken.getCode());
        verify(verificationTokenService).revokeToken(emailVerificationToken);
        verifyNoInteractions(genericVerificationProcessor);
    }

    @Test
    @DisplayName("Should throw TokenRevokedException for revoked code confirmation")
    void testConfirmCode_revokedCode() {
        emailVerificationToken.setRevoked(true);
        when(verificationTokenService.findByCode(emailVerificationToken.getCode()))
                .thenReturn(Optional.of(emailVerificationToken));

        TokenRevokedException exception = assertThrows(TokenRevokedException.class,
                () -> verificationProcessingService.confirmCode(emailVerificationToken.getCode()));

        assertThat(exception.getMessage()).isEqualTo(VerificationErrorMessages.CODE_REVOKED);
        verify(verificationTokenService).findByCode(emailVerificationToken.getCode());
        verify(verificationTokenService, never()).revokeToken(any());
        verifyNoInteractions(genericVerificationProcessor);
    }
}
