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
        String oldTelephone = user.getTelephone(); // Retrieve old telephone before updating

        // Send SMS notification to the old telephone number if it exists
        if (oldTelephone != null && !oldTelephone.isEmpty()) {
            smsService.sendMessage(oldTelephone, "Your telephone number has been changed from " + oldTelephone + " to " + newTelephone + ".");
        }

        // Send email notification to the user's email
        emailService.sendEmail(Email.builder()
                .to(user.getEmail())
                .subject("Telephone Number Changed")
                .template("email/telephone-changed-notification.html") // Assuming a new template for telephone changes
                .build());

        user.setTelephone(newTelephone);
        userService.update(user);

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
