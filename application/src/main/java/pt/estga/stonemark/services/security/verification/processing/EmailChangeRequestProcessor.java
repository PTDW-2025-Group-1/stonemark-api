package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.VerificationEmailService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class EmailChangeRequestProcessor implements VerificationProcessor {

    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;

    @Override
    public Optional<String> process(VerificationToken token) {
        EmailChangeRequest emailChangeRequest = emailChangeRequestRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = emailChangeRequest.getUser();
        String newEmail = emailChangeRequest.getNewEmail();

        // Create a new token for the confirmation of the email change
        VerificationToken confirmationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM);
        emailChangeRequest.setVerificationToken(confirmationToken);
        emailChangeRequestRepository.save(emailChangeRequest);

        verificationEmailService.sendVerificationEmail(newEmail, confirmationToken);

        // Revoke the current EMAIL_CHANGE_REQUEST token as it has served its purpose
        verificationTokenService.revokeToken(token);

        return Optional.empty();
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_REQUEST;
    }
}
