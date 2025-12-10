package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.UserNotFoundException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.repositories.UserContactRepository;

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
