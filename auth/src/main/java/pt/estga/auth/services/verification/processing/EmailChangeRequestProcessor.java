package pt.estga.auth.services.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.request.EmailChangeRequest;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.EmailChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.VerificationEmailService;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.user.entities.User;

import java.util.Optional;

/**
 * Processes verification tokens with the purpose {@link VerificationTokenPurpose#EMAIL_CHANGE_REQUEST}.
 * This processor initiates the email change process by creating a confirmation token
 * and sending a verification email to the new email address.
 */
@Component
@RequiredArgsConstructor
public class EmailChangeRequestProcessor implements VerificationProcessor {

    private final EmailChangeRequestRepository repository;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;

    /**
     * Processes the given verification token.
     * It retrieves the associated email change request, creates a new confirmation token,
     * sends a verification email to the new email address, and revokes the original token.
     *
     * @param token The verification token to process, expected to have purpose {@link VerificationTokenPurpose#EMAIL_CHANGE_REQUEST}.
     * @return An empty Optional, as this processor does not return a specific message.
     * @throws InvalidTokenException if the email change request associated with the token is not found.
     */
    @Override
    public Optional<String> process(VerificationToken token) {
        EmailChangeRequest request = repository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = request.getUser();
        String newEmail = request.getNewEmail();

        // Create a new token for the confirmation of the email change
        VerificationToken confirmationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM);
        request.setVerificationToken(confirmationToken);
        repository.save(request);

        verificationEmailService.sendVerificationEmail(newEmail, confirmationToken);

        // Revoke the token as it has served its purpose
        verificationTokenService.revokeToken(token);

        return Optional.empty();
    }

    /**
     * Returns the purpose of the verification token that this processor handles.
     *
     * @return The {@link VerificationTokenPurpose#EMAIL_CHANGE_REQUEST} purpose.
     */
    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_REQUEST;
    }
}
