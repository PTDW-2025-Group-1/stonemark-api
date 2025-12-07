package pt.estga.auth.services.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.request.TelephoneChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.TelephoneChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.services.SmsService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.service.UserService;

import java.util.Optional;

/**
 * Processes verification tokens with the purpose {@link VerificationTokenPurpose#TELEPHONE_CHANGE_CONFIRM}.
 * This processor finalizes the telephone change process by updating the user's telephone number,
 * sending a notification to the old telephone number (if available) and email, and cleaning up the request.
 */
@Component
@RequiredArgsConstructor
public class TelephoneChangeConfirmProcessor implements VerificationProcessor {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final TelephoneChangeRequestRepository repository;
    private final SmsService smsService; // Inject SmsService
    private final EmailService emailService; // Inject EmailService

    /**
     * Processes the given verification token.
     * It retrieves the associated telephone change request, updates the user's telephone,
     * sends notification messages, deletes the telephone change request, and revokes the token.
     *
     * @param token The verification token to process, expected to have purpose {@link VerificationTokenPurpose#TELEPHONE_CHANGE_CONFIRM}.
     * @return An empty Optional, as this processor does not return a specific message.
     * @throws InvalidTokenException if the telephone change request associated with the token is not found.
     */
    @Override
    public Optional<String> process(VerificationToken token) {
        TelephoneChangeRequest request = repository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Telephone change request not found."));

        User user = request.getUser();
        String newTelephone = request.getNewTelephone();

        Optional<UserContact> oldPrimaryTelephone = user.getContacts().stream()
                .filter(c -> c.getType() == ContactType.TELEPHONE && c.isPrimary())
                .findFirst();

        oldPrimaryTelephone.ifPresent(contact -> {
            contact.setPrimary(false);
            smsService.sendMessage(contact.getValue(), "Your telephone number has been changed from " + contact.getValue() + " to " + newTelephone + ".");
        });

        UserContact newContact = UserContact.builder()
                .user(user)
                .type(ContactType.TELEPHONE)
                .value(newTelephone)
                .primary(true)
                .verified(true)
                .build();

        user.getContacts().add(newContact);
        userService.update(user);

        user.getContacts().stream()
                .filter(c -> c.getType() == ContactType.EMAIL && c.isPrimary())
                .findFirst()
                .ifPresent(primaryEmail -> emailService.sendEmail(Email.builder()
                        .to(primaryEmail.getValue())
                        .subject("Telephone Number Changed")
                        .template("email/telephone-changed-notification.html") // Assuming a new template for telephone changes
                        .build()));

        repository.delete(request);
        verificationTokenService.revokeToken(token);

        return Optional.empty();
    }

    /**
     * Returns the purpose of the verification token that this processor handles.
     *
     * @return The {@link VerificationTokenPurpose#TELEPHONE_CHANGE_CONFIRM} purpose.
     */
    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TELEPHONE_CHANGE_CONFIRM;
    }
}
