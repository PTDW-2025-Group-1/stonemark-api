package pt.estga.verification.services.processors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VerificationProcessorPasswordResetImplTest {

    @InjectMocks
    private VerificationProcessorPasswordResetImpl verificationProcessorPasswordReset;

    private UserContact testUserContact;
    private ActionCode testActionCode;
    private final String TEST_CODE_STRING = "RESETCODE123";

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
                .code(TEST_CODE_STRING)
                .user(testUser)
                .type(ActionCodeType.RESET_PASSWORD)
                .build();
    }

    @Test
    void process_shouldReturnOptionalContainingCodeString() {
        Optional<String> result = verificationProcessorPasswordReset.process(testUserContact, testActionCode);

        assertTrue(result.isPresent());
        assertEquals(TEST_CODE_STRING, result.get());
    }

    @Test
    void process_shouldReturnOptionalContainingCodeString_whenUserContactIsNull() {
        // userContact is not used in the implementation, so passing null should not cause an issue
        Optional<String> result = verificationProcessorPasswordReset.process(null, testActionCode);

        assertTrue(result.isPresent());
        assertEquals(TEST_CODE_STRING, result.get());
    }

    @Test
    void getType_shouldReturnResetPassword() {
        assertEquals(ActionCodeType.RESET_PASSWORD, verificationProcessorPasswordReset.getType());
    }
}
