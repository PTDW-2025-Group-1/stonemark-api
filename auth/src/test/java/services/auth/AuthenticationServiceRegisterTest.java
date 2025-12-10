package services.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.ActionCodeCommand;
import pt.estga.auth.services.verification.commands.VerificationCommand;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserService;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceRegisterTest {

    @Mock
    private UserService userService;
    @Mock
    private JwtService jwtService;
    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private VerificationInitiationService verificationInitiationService;
    @Mock
    private ActionCodeService actionCodeService;
    @Mock
    private VerificationDispatchService verificationDispatchService;
    @Mock
    private UserContactService userContactService;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User testUser;
    private UserContact testUserContact;
    private final String testEmail = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username(testEmail)
                .password("password")
                .firstName("Test")
                .lastName("User")
                .role(Role.USER)
                .enabled(true)
                .tfaMethod(TfaMethod.NONE)
                .tfaSecret(null)
                .build();
        testUserContact = UserContact.builder()
                .id(1L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .isPrimary(true)
                .isVerified(true)
                .user(testUser)
                .build();
        testUser.setContacts(new ArrayList<>(List.of(testUserContact)));
    }

    private void setContactVerificationRequired(boolean value) {
        try {
            Field field = AuthenticationServiceSpringImpl.class.getDeclaredField("contactVerificationRequired");
            field.setAccessible(true);
            field.set(authenticationService, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set contactVerificationRequired field via reflection", e);
        }
    }

    @Test
    @DisplayName("Should register user and require email verification when enabled")
    void testRegister_success_emailVerificationRequired() {
        when(userService.existsByContactValue(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(testUser);
        when(userContactService.findPrimary(any(User.class), eq(ContactType.EMAIL))).thenReturn(Optional.of(testUserContact)); // Mock userContactService

        // Stub the verificationInitiationService.initiate method to execute the command
        doAnswer(invocation -> {
            VerificationCommand<Void> command = invocation.getArgument(0);
            command.execute(null);
            return null;
        }).when(verificationInitiationService).initiate(any(VerificationCommand.class));

        setContactVerificationRequired(true);

        assertThatExceptionOfType(EmailVerificationRequiredException.class)
                .isThrownBy(() -> authenticationService.register(testUser));

        verify(userService).existsByContactValue(testEmail);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isFalse();
        assertThat(userCaptor.getValue().getTfaMethod()).isEqualTo(TfaMethod.NONE);

        ArgumentCaptor<VerificationCommand<Void>> commandCaptor = ArgumentCaptor.forClass(VerificationCommand.class);
        verify(verificationInitiationService).initiate(commandCaptor.capture());
        assertThat(commandCaptor.getValue()).isInstanceOf(ActionCodeCommand.class);
        verify(actionCodeService).createAndSave(any(User.class), eq(ActionCodeType.EMAIL_VERIFICATION));
        verify(verificationDispatchService).sendVerification(eq(testUserContact), any());
        verify(userContactService).findPrimary(any(User.class), eq(ContactType.EMAIL));

        verifyNoInteractions(jwtService, accessTokenService, refreshTokenService);
    }

    @Test
    @DisplayName("Should register user and return tokens when email verification is not required")
    void testRegister_success_noEmailVerificationRequired() {
        when(userService.existsByContactValue(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userService.create(any(User.class))).thenReturn(testUser);
        when(jwtService.generateRefreshToken(testUser)).thenReturn("refreshTokenString");
        when(jwtService.generateAccessToken(testUser)).thenReturn("accessTokenString");
        when(refreshTokenService.createToken(anyString(), anyString())).thenReturn(null);
        when(accessTokenService.createToken(anyString(), anyString(), any())).thenReturn(null);

        setContactVerificationRequired(false);

        Optional<AuthenticationResponseDto> optionalResponse = authenticationService.register(testUser);

        assertThat(optionalResponse).isPresent();
        AuthenticationResponseDto response = optionalResponse.get();
        assertThat(response.accessToken()).isEqualTo("accessTokenString");
        assertThat(response.refreshToken()).isEqualTo("refreshTokenString");
        assertThat(response.tfaEnabled()).isFalse();
        assertThat(response.tfaRequired()).isFalse();
        assertThat(response.tfaCodeSent()).isFalse();

        verify(userService).existsByContactValue(testEmail);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService).create(userCaptor.capture());
        assertThat(userCaptor.getValue().isEnabled()).isTrue();
        assertThat(userCaptor.getValue().getTfaMethod()).isEqualTo(TfaMethod.NONE);


        verify(jwtService).generateRefreshToken(testUser);
        verify(jwtService).generateAccessToken(testUser);
        verify(refreshTokenService).createToken(anyString(), anyString());
        verify(accessTokenService).createToken(anyString(), anyString(), any());
        verifyNoInteractions(verificationInitiationService, actionCodeService, verificationDispatchService, userContactService);
    }

    @Test
    @DisplayName("Should throw EmailAlreadyTakenException if email exists")
    void testRegister_emailAlreadyTaken() {
        when(userService.existsByContactValue(testEmail)).thenReturn(true);

        assertThatExceptionOfType(EmailAlreadyTakenException.class)
                .isThrownBy(() -> authenticationService.register(testUser));

        verify(userService).existsByContactValue(testEmail);
        verifyNoMoreInteractions(userService);
        verifyNoInteractions(verificationInitiationService, actionCodeService, verificationDispatchService, userContactService);
    }
}
