package pt.estga.verification.services;

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
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserContactActivationServiceTest {

    @Mock
    private ActionCodeService actionCodeService;

    @Mock
    private UserContactRepository userContactRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserContactActivationService userContactActivationService;

    private User testUser;
    private UserContact emailContact;
    private UserContact phoneContact;
    private ActionCode emailActionCode;
    private ActionCode phoneActionCode;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .firstName("John")
                .username("john.doe")
                .build();

        emailContact = UserContact.builder()
                .id(10L)
                .user(testUser)
                .type(ContactType.EMAIL)
                .value("john.doe@example.com")
                .isVerified(false)
                .build();

        phoneContact = UserContact.builder()
                .id(11L)
                .user(testUser)
                .type(ContactType.TELEPHONE)
                .value("123456789")
                .isVerified(false)
                .build();

        List<UserContact> contacts = new ArrayList<>();
        contacts.add(emailContact);
        contacts.add(phoneContact);
        testUser.setContacts(contacts);

        emailActionCode = ActionCode.builder()
                .code("EMAILCODE")
                .user(testUser)
                .type(ActionCodeType.EMAIL_VERIFICATION)
                .build();

        phoneActionCode = ActionCode.builder()
                .code("PHONECODE")
                .user(testUser)
                .type(ActionCodeType.PHONE_VERIFICATION)
                .build();
    }

    @Test
    void activateUserContact_shouldActivateEmailContactAndSendEmail() {
        emailContact.setVerified(false);

        Optional<String> result = userContactActivationService.activateUserContact(emailActionCode);

        assertTrue(result.isEmpty());
        assertTrue(emailContact.isVerified());
        assertNotNull(emailContact.getVerifiedAt());
        verify(userContactRepository, times(1)).save(emailContact);
        verify(actionCodeService, times(1)).consumeCode(emailActionCode);
        verify(emailService, times(1)).sendEmail(any(Email.class));
    }

    @Test
    void activateUserContact_shouldActivatePhoneContactAndNotSendEmail() {
        phoneContact.setVerified(false);

        Optional<String> result = userContactActivationService.activateUserContact(phoneActionCode);

        assertTrue(result.isEmpty());
        assertTrue(phoneContact.isVerified());
        assertNotNull(phoneContact.getVerifiedAt());
        verify(userContactRepository, times(1)).save(phoneContact);
        verify(actionCodeService, times(1)).consumeCode(phoneActionCode);
        verifyNoInteractions(emailService);
    }

    @Test
    void activateUserContact_shouldConsumeCode_whenContactAlreadyVerified() {
        emailContact.setVerified(true);
        testUser.setContacts(Collections.singletonList(emailContact));

        Optional<String> result = userContactActivationService.activateUserContact(emailActionCode);

        assertTrue(result.isEmpty());
        verify(actionCodeService, times(1)).consumeCode(emailActionCode);
        verify(userContactRepository, never()).save(any(UserContact.class));
        verifyNoInteractions(emailService);
    }

    @Test
    void activateUserContact_shouldThrowIllegalStateException_whenNoMatchingContactFound() {
        // Set contacts to an empty list or a list that doesn't contain the expected type
        testUser.setContacts(Collections.emptyList()); // No email contact for the action code

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> userContactActivationService.activateUserContact(emailActionCode));

        assertEquals("No matching contact found for the action code.", thrown.getMessage());
        verifyNoInteractions(userContactRepository, actionCodeService, emailService);
    }

    @Test
    void activateUserContact_shouldThrowIllegalArgumentException_forUnsupportedActionCodeType() {
        ActionCode unsupportedActionCode = ActionCode.builder()
                .code("UNSUPPORTED")
                .user(testUser)
                .type(ActionCodeType.RESET_PASSWORD)
                .build();

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> userContactActivationService.activateUserContact(unsupportedActionCode));

        assertEquals("Unsupported action code type for contact activation: RESET_PASSWORD", thrown.getMessage());
        verifyNoInteractions(userContactRepository, actionCodeService, emailService);
    }

    @Test
    void activateUserContact_shouldPropagateException_whenUserContactRepositoryFails() {
        doThrow(new RuntimeException("DB error")).when(userContactRepository).save(any(UserContact.class));

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> userContactActivationService.activateUserContact(emailActionCode));

        assertEquals("DB error", thrown.getMessage());
        verify(userContactRepository, times(1)).save(emailContact);
        verifyNoInteractions(actionCodeService, emailService);
    }
}
