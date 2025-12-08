package services.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.auth.services.AuthenticationServiceSpringImpl;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.VerificationProcessingService;
import pt.estga.auth.services.verification.commands.VerificationCommand;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Role;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.service.UserService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServicePasswordResetTest {

    @Mock
    private UserService userService;
    @Mock
    private VerificationInitiationService verificationInitiationService;
    @Mock
    private VerificationCommandFactory verificationCommandFactory;
    @Mock
    private VerificationProcessingService verificationProcessingService;

    @InjectMocks
    private AuthenticationServiceSpringImpl authenticationService;

    private User testUser;
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
        UserContact testUserContact = UserContact.builder()
                .id(1L)
                .type(ContactType.EMAIL)
                .value(testEmail)
                .primary(true)
                .verified(true)
                .user(testUser)
                .build();
        testUser.setContacts(new ArrayList<>(List.of(testUserContact)));
    }

    @Test
    @DisplayName("Should initiate password reset request")
    void testRequestPasswordReset_success() {
        when(userService.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(verificationCommandFactory.createPasswordResetCommand(any(User.class)))
                .thenReturn(mock(VerificationCommand.class));

        authenticationService.requestPasswordReset(testEmail);

        verify(userService).findByEmail(testEmail);
        verify(verificationCommandFactory).createPasswordResetCommand(testUser);
        verify(verificationInitiationService).initiate(any(VerificationCommand.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException if user not found for password reset")
    void testRequestPasswordReset_userNotFound() {
        String email = "nonexistent@example.com";
        when(userService.findByEmail(email)).thenReturn(Optional.empty());

        assertThatExceptionOfType(IllegalArgumentException.class)
                .isThrownBy(() -> authenticationService.requestPasswordReset(email))
                .withMessage("User not found");

        verify(userService).findByEmail(email);
        verifyNoInteractions(verificationCommandFactory, verificationInitiationService);
    }

    @Test
    @DisplayName("Should delegate password reset to verification processing service")
    void testResetPassword_success() {
        String token = "validToken";
        String newPassword = "newPassword";

        authenticationService.resetPassword(token, newPassword);

        verify(verificationProcessingService).processPasswordReset(token, newPassword);
    }
}
