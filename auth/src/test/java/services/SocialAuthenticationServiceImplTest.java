package services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pt.estga.auth.dtos.AuthenticationResponseDto;
import pt.estga.auth.services.JwtService;
import pt.estga.auth.services.SocialAuthenticationServiceImpl;
import pt.estga.auth.services.token.AccessTokenService;
import pt.estga.auth.services.token.RefreshTokenService;
import pt.estga.shared.exceptions.EmailVerificationRequiredException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserIdentityService;
import pt.estga.user.services.UserService;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialAuthenticationServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private UserIdentityService userIdentityService;
    @Mock
    private GoogleIdTokenVerifier googleIdTokenVerifier;
    @Mock
    private JwtService jwtService;
    @Mock
    private AccessTokenService accessTokenService;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private GoogleIdToken googleIdToken;
    @Mock
    private GoogleIdToken.Payload payload;

    @InjectMocks
    private SocialAuthenticationServiceImpl socialAuthenticationService;

    private final String GOOGLE_TOKEN = "someGoogleToken";
    private final String GOOGLE_ID = "googleSubId";
    private final String USER_EMAIL = "test@example.com";
    private final String FIRST_NAME = "Test";
    private final String LAST_NAME = "User";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(socialAuthenticationService, "emailVerificationRequired", false);
    }

    private User createTestUser(boolean enabled) {
        User user = User.builder()
                .id(1L)
                .username(USER_EMAIL)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .role(Role.USER)
                .enabled(enabled)
                .tfaMethod(TfaMethod.NONE)
                .identities(new ArrayList<>())
                .contacts(new ArrayList<>())
                .build();
        UserContact primaryEmail = UserContact.builder()
                .type(ContactType.EMAIL)
                .value(USER_EMAIL)
                .isPrimary(true)
                .isVerified(true)
                .user(user)
                .build();
        user.getContacts().add(primaryEmail);
        return user;
    }

    @Test
    void authenticateWithGoogle_shouldReturnAuthResponse_whenNewUser() throws GeneralSecurityException, IOException {
        User newUser = createTestUser(true);
        UserIdentity newIdentity = UserIdentity.builder().provider(Provider.GOOGLE).identity(GOOGLE_ID).user(newUser).build();

        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(USER_EMAIL);
        when(payload.getSubject()).thenReturn(GOOGLE_ID);
        when(payload.get("given_name")).thenReturn(FIRST_NAME);
        when(payload.get("family_name")).thenReturn(LAST_NAME);

        when(userIdentityService.findByProviderAndIdentity(Provider.GOOGLE, GOOGLE_ID)).thenReturn(Optional.empty());
        when(userService.findByContact(USER_EMAIL)).thenReturn(Optional.empty());
        when(userService.create(any(User.class))).thenReturn(newUser);
        when(userIdentityService.createAndAssociateUserIdentity(any(User.class), eq(Provider.GOOGLE), eq(GOOGLE_ID))).thenReturn(newIdentity);

        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN);

        assertTrue(response.isPresent());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
        verify(userService, times(1)).create(any(User.class));
        verify(userIdentityService, times(1)).createAndAssociateUserIdentity(any(User.class), eq(Provider.GOOGLE), eq(GOOGLE_ID));
    }

    @Test
    void authenticateWithGoogle_shouldReturnAuthResponse_whenExistingUser() throws GeneralSecurityException, IOException {
        User existingUser = createTestUser(true);
        UserIdentity existingIdentity = UserIdentity.builder().provider(Provider.GOOGLE).identity(GOOGLE_ID).user(existingUser).build();

        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(USER_EMAIL);
        when(payload.getSubject()).thenReturn(GOOGLE_ID);

        when(userIdentityService.findByProviderAndIdentity(Provider.GOOGLE, GOOGLE_ID)).thenReturn(Optional.of(existingIdentity));

        when(jwtService.generateAccessToken(any())).thenReturn("accessToken");
        when(jwtService.generateRefreshToken(any())).thenReturn("refreshToken");

        Optional<AuthenticationResponseDto> response = socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN);

        assertTrue(response.isPresent());
        assertEquals("accessToken", response.get().accessToken());
        assertEquals("refreshToken", response.get().refreshToken());
        verify(userService, never()).create(any(User.class));
        verify(userIdentityService, never()).createAndAssociateUserIdentity(any(User.class), any(Provider.class), anyString());
        verify(userService, never()).update(any(User.class));
    }

    @Test
    void authenticateWithGoogle_shouldReturnEmpty_whenInvalidGoogleToken() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenReturn(null);

        Optional<AuthenticationResponseDto> response = socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN);

        assertFalse(response.isPresent());
        verifyNoInteractions(userService, userIdentityService, jwtService, accessTokenService, refreshTokenService);
    }

    @Test
    void authenticateWithGoogle_shouldThrowRuntimeException_whenGeneralSecurityExceptionOccurs() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenThrow(new GeneralSecurityException("Test security exception"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN));
        assertTrue(thrown.getMessage().contains("Google authentication failed."));
        assertInstanceOf(GeneralSecurityException.class, thrown.getCause());
    }

    @Test
    void authenticateWithGoogle_shouldThrowRuntimeException_whenIOExceptionOccurs() throws GeneralSecurityException, IOException {
        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenThrow(new IOException("Test IO exception"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN));
        assertTrue(thrown.getMessage().contains("Google authentication failed."));
        assertInstanceOf(IOException.class, thrown.getCause());
    }

    @Test
    void authenticateWithGoogle_shouldThrowEmailVerificationRequiredException_whenEmailVerificationRequiredAndUserNotEnabled() throws GeneralSecurityException, IOException {
        ReflectionTestUtils.setField(socialAuthenticationService, "emailVerificationRequired", true);
        User newUser = createTestUser(false);
        UserIdentity newIdentity = UserIdentity.builder().provider(Provider.GOOGLE).identity(GOOGLE_ID).user(newUser).build();

        when(googleIdTokenVerifier.verify(GOOGLE_TOKEN)).thenReturn(googleIdToken);
        when(googleIdToken.getPayload()).thenReturn(payload);
        when(payload.getEmail()).thenReturn(USER_EMAIL);
        when(payload.getSubject()).thenReturn(GOOGLE_ID);
        when(payload.get("given_name")).thenReturn(FIRST_NAME);
        when(payload.get("family_name")).thenReturn(LAST_NAME);

        when(userIdentityService.findByProviderAndIdentity(Provider.GOOGLE, GOOGLE_ID)).thenReturn(Optional.empty());
        when(userService.findByContact(USER_EMAIL)).thenReturn(Optional.empty());
        when(userService.create(any(User.class))).thenReturn(newUser);
        when(userIdentityService.createAndAssociateUserIdentity(any(User.class), eq(Provider.GOOGLE), eq(GOOGLE_ID))).thenReturn(newIdentity);

        EmailVerificationRequiredException thrown = assertThrows(EmailVerificationRequiredException.class,
                () -> socialAuthenticationService.authenticateWithGoogle(GOOGLE_TOKEN));
        assertTrue(thrown.getMessage().contains("Email verification required."));
        verify(userService, times(1)).create(any(User.class));
        verify(userIdentityService, times(1)).createAndAssociateUserIdentity(any(User.class), eq(Provider.GOOGLE), eq(GOOGLE_ID));
    }

    @Test
    void authenticateWithTelegram_shouldReturnEmptyAndLogWarning() {
        Optional<AuthenticationResponseDto> response = socialAuthenticationService.authenticateWithTelegram("someTelegramData");

        assertFalse(response.isPresent());
    }
}
