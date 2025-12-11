package pt.estga.verification.services.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessorEmailImplTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private VerificationProcessorEmailImpl verificationProcessorEmail;

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
                .code("EMAILCODE")
                .type(ActionCodeType.EMAIL_VERIFICATION)
                .build();
    }

    @Test
    void process_shouldSendEmailAndReturnEmptyOptional() {
        Optional<String> result = verificationProcessorEmail.process(testUserContact, testActionCode);

        assertTrue(result.isEmpty());
        verify(emailService, times(1)).sendEmail(any(Email.class));
    }

    @Test
    void process_shouldThrowIllegalArgumentException_whenUserContactIsNull() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> verificationProcessorEmail.process(null, testActionCode));

        assertEquals("UserContact cannot be null for email verification.", thrown.getMessage());
        verifyNoInteractions(emailService);
    }

    @Test
    void process_shouldPropagateException_whenEmailServiceFails() {
        doThrow(new RuntimeException("Email service error")).when(emailService).sendEmail(any(Email.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> verificationProcessorEmail.process(testUserContact, testActionCode));

        assertEquals("Email service error", thrown.getMessage());
        verify(emailService, times(1)).sendEmail(any(Email.class));
    }

    @Test
    void getType_shouldReturnEmailVerification() {
        assertEquals(ActionCodeType.EMAIL_VERIFICATION, verificationProcessorEmail.getType());
    }
}
