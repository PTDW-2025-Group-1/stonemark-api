package pt.estga.verification.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.services.processors.VerificationProcessor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActionCodeDispatchServiceTest {

    @Mock
    private VerificationProcessor emailVerificationProcessor;

    @Mock
    private VerificationProcessor phoneVerificationProcessor;

    @InjectMocks
    private ActionCodeDispatchServiceImpl actionCodeDispatchService;

    private UserContact testUserContact;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder().id(1L).username("testuser").build();
        testUserContact = UserContact.builder().id(1L).user(testUser).value("test@example.com").build();

        // Configure mock processors
        when(emailVerificationProcessor.getType()).thenReturn(ActionCodeType.EMAIL_VERIFICATION);
        when(phoneVerificationProcessor.getType()).thenReturn(ActionCodeType.PHONE_VERIFICATION);

        // Manually inject the list of processors and call init()
        actionCodeDispatchService = new ActionCodeDispatchServiceImpl(List.of(emailVerificationProcessor, phoneVerificationProcessor));
        actionCodeDispatchService.init();

        // Clear invocations after init to avoid interference with verifyNoInteractions
        clearInvocations(emailVerificationProcessor, phoneVerificationProcessor);
    }

    @Test
    void sendVerification_shouldDispatchToCorrectProcessor_forEmailVerification() {
        ActionCode emailCode = ActionCode.builder().type(ActionCodeType.EMAIL_VERIFICATION).build();

        actionCodeDispatchService.sendVerification(testUserContact, emailCode);

        verify(emailVerificationProcessor, times(1)).process(testUserContact, emailCode);
        verifyNoInteractions(phoneVerificationProcessor);
    }

    @Test
    void sendVerification_shouldDispatchToCorrectProcessor_forPhoneVerification() {
        ActionCode phoneCode = ActionCode.builder().type(ActionCodeType.PHONE_VERIFICATION).build();

        actionCodeDispatchService.sendVerification(testUserContact, phoneCode);

        verify(phoneVerificationProcessor, times(1)).process(testUserContact, phoneCode);
        verifyNoInteractions(emailVerificationProcessor);
    }

    @Test
    void sendVerification_shouldNotDispatch_whenNoProcessorFound() {
        ActionCode unknownTypeCode = ActionCode.builder().type(ActionCodeType.RESET_PASSWORD).build(); // Assuming RESET_PASSWORD has no processor in this setup

        actionCodeDispatchService.sendVerification(testUserContact, unknownTypeCode);

        verifyNoInteractions(emailVerificationProcessor, phoneVerificationProcessor);
    }

    @Test
    void sendVerification_shouldPropagateException_whenProcessorThrowsException() {
        ActionCode emailCode = ActionCode.builder().type(ActionCodeType.EMAIL_VERIFICATION).build();
        doThrow(new RuntimeException("Processor failed")).when(emailVerificationProcessor).process(testUserContact, emailCode);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> actionCodeDispatchService.sendVerification(testUserContact, emailCode));

        assertEquals("Processor failed", thrown.getMessage());
        verify(emailVerificationProcessor, times(1)).process(testUserContact, emailCode);
        verifyNoInteractions(phoneVerificationProcessor);
    }
}
