package pt.estga.auth.services.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.models.Email;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.repositories.EmailChangeRequestRepository;
import pt.estga.user.entities.User;
import pt.estga.auth.entities.request.EmailChangeRequest;
import pt.estga.user.service.UserService;

import java.util.Optional;

/**
 * Processes verification tokens with the purpose {@link VerificationTokenPurpose#EMAIL_CHANGE_CONFIRM}.
 * This processor finalizes the email change process by updating the user's email address,
 * sending a notification to the old email, and cleaning up the request.
 */
@Component
@RequiredArgsConstructor
public class EmailChangeConfirmProcessor implements VerificationProcessor {

    private final EmailChangeRequestRepository repository;
    private final UserService userService;
    private final EmailService emailService;
    private final VerificationTokenService tokenService;

    /**
     * Processes the given verification token.
     * It retrieves the associated email change request, updates the user's email,
     * sends a notification email, deletes the email change request, and revokes the token.
     *
     * @param token The verification token to process, expected to have purpose {@link VerificationTokenPurpose#EMAIL_CHANGE_CONFIRM}.
     * @return An empty Optional, as this processor does not return a specific message.
     * @throws InvalidTokenException if the email change request associated with the token is not found.
     */
    @Override
    public Optional<String> process(VerificationToken token) {
        EmailChangeRequest request = repository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = request.getUser();
        String newEmail = request.getNewEmail();

        emailService.sendEmail(Email.builder()
                .to(user.getEmail())
                .subject("Email Address Changed")
                .template("email/email-changed-notification.html")
                .build());

        user.setEmail(newEmail);
        user.setGoogleId(null);
        userService.update(user);

        repository.delete(request);
        tokenService.revokeToken(token);

        return Optional.empty();
    }

    /**
     * Returns the purpose of the verification token that this processor handles.
     *
     * @return The {@link VerificationTokenPurpose#EMAIL_CHANGE_CONFIRM} purpose.
     */
    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM;
    }
}
