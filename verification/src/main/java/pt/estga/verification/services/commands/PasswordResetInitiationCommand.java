package pt.estga.verification.services.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.UserNotFoundException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.services.ActionCodeService;
import pt.estga.verification.services.VerificationDispatchService;

@Component
@RequiredArgsConstructor
public class PasswordResetInitiationCommand implements VerificationCommand<String> {

    private final UserContactRepository userContactRepository;
    private final ActionCodeService actionCodeService;
    private final VerificationDispatchService verificationDispatchService;

    @Override
    public void execute(String contactValue) {
        UserContact userContact = userContactRepository.findByValue(contactValue)
                .orElseThrow(() -> new UserNotFoundException("User not found with contact: " + contactValue));

        User user = userContact.getUser();
        if (!user.isEnabled()) {
            throw new UserNotFoundException("User not found with contact: " + contactValue);
        }

        if (!userContact.isVerified()) {
            throw new ContactMethodNotAvailableException("Contact is not verified: " + contactValue);
        }

        ActionCode actionCode = actionCodeService.createAndSave(user, ActionCodeType.RESET_PASSWORD);

        verificationDispatchService.sendVerification(userContact, actionCode);
    }
}
