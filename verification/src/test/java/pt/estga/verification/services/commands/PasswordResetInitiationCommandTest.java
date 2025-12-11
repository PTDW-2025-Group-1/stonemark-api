package pt.estga.verification.services.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.UserNotFoundException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.services.ActionCodeService;
import pt.estga.verification.services.VerificationDispatchService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetInitiationCommandTest {

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private ActionCodeService actionCodeService;

    @Mock
    private VerificationDispatchService verificationDispatchService;

    @InjectMocks
    private PasswordResetInitiationCommand passwordResetInitiationCommand;

    private User testUser;
    private UserContact testUserContact;
    private ActionCode testActionCode;
    private final String CONTACT_VALUE = "test@example.com";

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .enabled(true)
                .build();

        testUserContact = UserContact.builder()
                .id(10L)
                .user(testUser)
                .type(ContactType.EMAIL)
                .value(CONTACT_VALUE)
                .isVerified(true)
                .build();

        testActionCode = ActionCode.builder()
                .id(100L)
                .code("RESETCODE")
                .user(testUser)
                .type(ActionCodeType.RESET_PASSWORD)
                .build();
    }

    @Test
    void execute_shouldReturnRunnableThatDispatchesVerification_whenContactIsValid() {
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.of(testUserContact));
        when(actionCodeService.createAndSave(testUser, ActionCodeType.RESET_PASSWORD)).thenReturn(testActionCode);

        Runnable runnable = passwordResetInitiationCommand.execute(CONTACT_VALUE);
        assertNotNull(runnable);

        // Execute the runnable to trigger the dispatch
        runnable.run();

        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verify(actionCodeService, times(1)).createAndSave(testUser, ActionCodeType.RESET_PASSWORD);
        verify(verificationDispatchService, times(1)).sendVerification(testUserContact, testActionCode);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenContactNotFound() {
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.empty());

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class,
                () -> passwordResetInitiationCommand.execute(CONTACT_VALUE));

        assertEquals("User not found with contact: " + CONTACT_VALUE, thrown.getMessage());
        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verifyNoInteractions(actionCodeService, verificationDispatchService);
    }

    @Test
    void execute_shouldThrowUserNotFoundException_whenUserIsNotEnabled() {
        testUser.setEnabled(false); // User is not enabled
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.of(testUserContact));

        UserNotFoundException thrown = assertThrows(UserNotFoundException.class,
                () -> passwordResetInitiationCommand.execute(CONTACT_VALUE));

        assertEquals("User not found with contact: " + CONTACT_VALUE, thrown.getMessage());
        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verifyNoInteractions(actionCodeService, verificationDispatchService);
    }

    @Test
    void execute_shouldThrowContactMethodNotAvailableException_whenContactIsNotVerified() {
        testUserContact.setVerified(false);
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.of(testUserContact));

        ContactMethodNotAvailableException thrown = assertThrows(ContactMethodNotAvailableException.class,
                () -> passwordResetInitiationCommand.execute(CONTACT_VALUE));

        assertEquals("Contact is not verified: " + CONTACT_VALUE, thrown.getMessage());
        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verifyNoInteractions(actionCodeService, verificationDispatchService);
    }

    @Test
    void execute_shouldPropagateException_whenActionCodeServiceFails() {
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.of(testUserContact));
        when(actionCodeService.createAndSave(testUser, ActionCodeType.RESET_PASSWORD))
                .thenThrow(new RuntimeException("ActionCodeService error"));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> passwordResetInitiationCommand.execute(CONTACT_VALUE));

        assertEquals("ActionCodeService error", thrown.getMessage());
        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verify(actionCodeService, times(1)).createAndSave(testUser, ActionCodeType.RESET_PASSWORD);
        verifyNoInteractions(verificationDispatchService);
    }

    @Test
    void execute_shouldPropagateException_whenDispatchServiceFailsDuringRunnableExecution() {
        when(userContactRepository.findByValue(CONTACT_VALUE)).thenReturn(Optional.of(testUserContact));
        when(actionCodeService.createAndSave(testUser, ActionCodeType.RESET_PASSWORD)).thenReturn(testActionCode);
        doThrow(new RuntimeException("Dispatch error")).when(verificationDispatchService)
                .sendVerification(testUserContact, testActionCode);

        Runnable runnable = passwordResetInitiationCommand.execute(CONTACT_VALUE);
        assertNotNull(runnable);

        RuntimeException thrown = assertThrows(RuntimeException.class, runnable::run);

        assertEquals("Dispatch error", thrown.getMessage());
        verify(userContactRepository, times(1)).findByValue(CONTACT_VALUE);
        verify(actionCodeService, times(1)).createAndSave(testUser, ActionCodeType.RESET_PASSWORD);
        verify(verificationDispatchService, times(1)).sendVerification(testUserContact, testActionCode);
    }
}
