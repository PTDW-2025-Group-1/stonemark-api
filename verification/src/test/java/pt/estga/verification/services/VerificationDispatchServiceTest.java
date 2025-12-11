package pt.estga.verification.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationDispatchServiceTest {

    @Mock
    private ActionCodeDispatchService actionCodeDispatchService;

    @InjectMocks
    private VerificationDispatchServiceImpl verificationDispatchService;

    private UserContact testUserContact;
    private ActionCode testActionCode;

    @BeforeEach
    void setUp() {
        User testUser = User.builder().id(1L).username("testuser").build();
        testUserContact = UserContact.builder()
                .id(1L)
                .user(testUser)
                .type(ContactType.EMAIL)
                .value("test@example.com")
                .build();
        testActionCode = ActionCode.builder()
                .id(10L)
                .code("CODE123")
                .type(ActionCodeType.EMAIL_VERIFICATION)
                .build();
    }

    @Test
    void sendVerification_shouldDelegateToActionCodeDispatchService() {
        verificationDispatchService.sendVerification(testUserContact, testActionCode);

        verify(actionCodeDispatchService, times(1))
                .sendVerification(testUserContact, testActionCode);
    }

    @Test
    void sendVerification_shouldPropagateException_whenActionCodeDispatchServiceThrowsException() {
        doThrow(new RuntimeException("Dispatch failed"))
                .when(actionCodeDispatchService)
                .sendVerification(any(UserContact.class), any(ActionCode.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> verificationDispatchService.sendVerification(testUserContact, testActionCode));

        assertEquals("Dispatch failed", thrown.getMessage());
        verify(actionCodeDispatchService, times(1))
                .sendVerification(testUserContact, testActionCode);
    }
}
