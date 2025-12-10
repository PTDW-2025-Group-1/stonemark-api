package services.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.tfa.ContactBasedTwoFactorAuthenticationService;
import pt.estga.auth.services.tfa.TwoFactorAuthenticationService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTwoFactorAuthenticationTest {

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
    private TwoFactorAuthenticationService twoFactorAuthenticationService;
    @Mock
    private ContactBasedTwoFactorAuthenticationService contactBasedTwoFactorAuthenticationService;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User tfaEnabledUserTotp;
    private User tfaEnabledUserSms;
    private User tfaEnabledUserEmail;
    private final String tfaSecret = "TFA_SECRET_KEY";
    private final String tfaTelephone = "+351912345678";


    @BeforeEach
    void setUp() {
        tfaEnabledUserTotp = User.builder()
                .id(3L)
                .username("tfa.totp@example.com")
                .password("password")
                .firstName("TfaTotp")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaMethod(TfaMethod.TOTP)
                .tfaSecret(tfaSecret)
                .build();
        UserContact tfaTotpUserContact = UserContact.builder()
                .id(3L)
                .type(ContactType.EMAIL)
                .value("tfa.totp@example.com")
                .isPrimary(true)
                .isVerified(true)
                .user(tfaEnabledUserTotp)
                .build();
        tfaEnabledUserTotp.setContacts(new ArrayList<>(List.of(tfaTotpUserContact)));

        tfaEnabledUserSms = User.builder()
                .id(4L)
                .username("tfa.sms@example.com")
                .password("password")
                .firstName("TfaSms")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaMethod(TfaMethod.SMS)
                .tfaSecret(null)
                .build();
        UserContact tfaSmsUserContactEmail = UserContact.builder()
                .id(4L)
                .type(ContactType.EMAIL)
                .value("tfa.sms@example.com")
                .isPrimary(true)
                .isVerified(true)
                .user(tfaEnabledUserSms)
                .build();
        UserContact tfaSmsUserContactPhone = UserContact.builder()
                .id(5L)
                .type(ContactType.TELEPHONE)
                .value(tfaTelephone)
                .isPrimary(true)
                .isVerified(true)
                .user(tfaEnabledUserSms)
                .build();
        tfaEnabledUserSms.setContacts(new ArrayList<>(List.of(tfaSmsUserContactEmail, tfaSmsUserContactPhone)));

        tfaEnabledUserEmail = User.builder()
                .id(5L)
                .username("tfa.email@example.com")
                .password("password")
                .firstName("TfaEmail")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaMethod(TfaMethod.EMAIL)
                .tfaSecret(null)
                .build();
        UserContact tfaEmailUserContact = UserContact.builder()
                .id(6L)
                .type(ContactType.EMAIL)
                .value("tfa.email@example.com")
                .isPrimary(true)
                .isVerified(true)
                .user(tfaEnabledUserEmail)
                .build();
        tfaEnabledUserEmail.setContacts(new ArrayList<>(List.of(tfaEmailUserContact)));
    }

    private void setContactVerificationRequired(boolean value) {
        try {
            Field field = AuthenticationServiceSpringImpl.class.getDeclaredField("contactVerificationRequired");
            field.setAccessible(true);
            field.set(authenticationService, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set emailVerificationRequired field via reflection", e);
        }
    }

    @Test
    @DisplayName("Should return 2FA required when TOTP is enabled but no code is provided")
    void testAuthenticate_tfaEnabledTotp_tfaCodeRequired() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserTotp.getUsername())).thenReturn(Optional.of(tfaEnabledUserTotp));

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserTotp.getUsername(), tfaEnabledUserTotp.getPassword(), null);

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isNull();
        assertThat(response.get().refreshToken()).isNull();
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isTrue();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserTotp.getUsername());
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService);
        verifyNoInteractions(twoFactorAuthenticationService, contactBasedTwoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return empty optional for invalid TOTP 2FA code")
    void testAuthenticate_tfaEnabledTotp_invalidTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserTotp.getUsername())).thenReturn(Optional.of(tfaEnabledUserTotp));
        when(twoFactorAuthenticationService.isCodeValid(tfaSecret, "invalidCode")).thenReturn(false);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserTotp.getUsername(), tfaEnabledUserTotp.getPassword(), "invalidCode");

        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserTotp.getUsername());
        verify(twoFactorAuthenticationService).isCodeValid(tfaSecret, "invalidCode");
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService, contactBasedTwoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should authenticate successfully with valid TOTP 2FA code")
    void testAuthenticate_tfaEnabledTotp_validTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserTotp.getUsername())).thenReturn(Optional.of(tfaEnabledUserTotp));
        when(twoFactorAuthenticationService.isCodeValid(tfaSecret, "validCode")).thenReturn(true);
        when(jwtService.generateRefreshToken(tfaEnabledUserTotp)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(tfaEnabledUserTotp)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserTotp.getUsername(), tfaEnabledUserTotp.getPassword(), "validCode");

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isFalse();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserTotp.getUsername());
        verify(twoFactorAuthenticationService).isCodeValid(tfaSecret, "validCode");
        verify(jwtService).generateRefreshToken(tfaEnabledUserTotp);
        verify(jwtService).generateAccessToken(tfaEnabledUserTotp);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(contactBasedTwoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return 2FA required and code sent when SMS is enabled but no code is provided")
    void testAuthenticate_tfaEnabledSms_tfaCodeRequired() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserSms.getUsername())).thenReturn(Optional.of(tfaEnabledUserSms));
        doNothing().when(contactBasedTwoFactorAuthenticationService).generateAndSendSmsCode(tfaEnabledUserSms);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserSms.getUsername(), tfaEnabledUserSms.getPassword(), null);

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isNull();
        assertThat(response.get().refreshToken()).isNull();
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isTrue();
        assertThat(response.get().tfaCodeSent()).isTrue();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserSms.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).generateAndSendSmsCode(tfaEnabledUserSms);
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService);
        verifyNoInteractions(twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return empty optional for invalid SMS 2FA code")
    void testAuthenticate_tfaEnabledSms_invalidTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserSms.getUsername())).thenReturn(Optional.of(tfaEnabledUserSms));
        when(contactBasedTwoFactorAuthenticationService.verifyCode(tfaEnabledUserSms, "invalidCode", ActionCodeType.PHONE_VERIFICATION)).thenReturn(false);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserSms.getUsername(), tfaEnabledUserSms.getPassword(), "invalidCode");

        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserSms.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).verifyCode(tfaEnabledUserSms, "invalidCode", ActionCodeType.PHONE_VERIFICATION);
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService, twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should authenticate successfully with valid SMS 2FA code")
    void testAuthenticate_tfaEnabledSms_validTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserSms.getUsername())).thenReturn(Optional.of(tfaEnabledUserSms));
        when(contactBasedTwoFactorAuthenticationService.verifyCode(tfaEnabledUserSms, "validCode", ActionCodeType.PHONE_VERIFICATION)).thenReturn(true);
        when(jwtService.generateRefreshToken(tfaEnabledUserSms)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(tfaEnabledUserSms)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserSms.getUsername(), tfaEnabledUserSms.getPassword(), "validCode");

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isFalse();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserSms.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).verifyCode(tfaEnabledUserSms, "validCode", ActionCodeType.PHONE_VERIFICATION);
        verify(jwtService).generateRefreshToken(tfaEnabledUserSms);
        verify(jwtService).generateAccessToken(tfaEnabledUserSms);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return 2FA required and code sent when Email is enabled but no code is provided")
    void testAuthenticate_tfaEnabledEmail_tfaCodeRequired() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserEmail.getUsername())).thenReturn(Optional.of(tfaEnabledUserEmail));
        doNothing().when(contactBasedTwoFactorAuthenticationService).generateAndSendEmailCode(tfaEnabledUserEmail);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserEmail.getUsername(), tfaEnabledUserEmail.getPassword(), null);

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isNull();
        assertThat(response.get().refreshToken()).isNull();
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isTrue();
        assertThat(response.get().tfaCodeSent()).isTrue();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserEmail.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).generateAndSendEmailCode(tfaEnabledUserEmail);
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService);
        verifyNoInteractions(twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should return empty optional for invalid Email 2FA code")
    void testAuthenticate_tfaEnabledEmail_invalidTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserEmail.getUsername())).thenReturn(Optional.of(tfaEnabledUserEmail));
        when(contactBasedTwoFactorAuthenticationService.verifyCode(tfaEnabledUserEmail, "invalidCode", ActionCodeType.EMAIL_VERIFICATION)).thenReturn(false);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserEmail.getUsername(), tfaEnabledUserEmail.getPassword(), "invalidCode");

        assertThat(response).isNotPresent();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserEmail.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).verifyCode(tfaEnabledUserEmail, "invalidCode", ActionCodeType.EMAIL_VERIFICATION);
        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService, twoFactorAuthenticationService);
    }

    @Test
    @DisplayName("Should authenticate successfully with valid Email 2FA code")
    void testAuthenticate_tfaEnabledEmail_validTfaCode() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(mock(Authentication.class));
        when(userService.findByContact(tfaEnabledUserEmail.getUsername())).thenReturn(Optional.of(tfaEnabledUserEmail));
        when(contactBasedTwoFactorAuthenticationService.verifyCode(tfaEnabledUserEmail, "validCode", ActionCodeType.EMAIL_VERIFICATION)).thenReturn(true);
        when(jwtService.generateRefreshToken(tfaEnabledUserEmail)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(tfaEnabledUserEmail)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> response = authenticationService.authenticate(tfaEnabledUserEmail.getUsername(), tfaEnabledUserEmail.getPassword(), "validCode");

        assertThat(response).isPresent();
        assertThat(response.get().accessToken()).isEqualTo("accessTokenString");
        assertThat(response.get().refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.get().tfaEnabled()).isTrue();
        assertThat(response.get().tfaRequired()).isFalse();
        assertThat(response.get().tfaCodeSent()).isFalse();
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userService).findByContact(tfaEnabledUserEmail.getUsername());
        verify(contactBasedTwoFactorAuthenticationService).verifyCode(tfaEnabledUserEmail, "validCode", ActionCodeType.EMAIL_VERIFICATION);
        verify(jwtService).generateRefreshToken(tfaEnabledUserEmail);
        verify(jwtService).generateAccessToken(tfaEnabledUserEmail);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(twoFactorAuthenticationService);
    }
}
