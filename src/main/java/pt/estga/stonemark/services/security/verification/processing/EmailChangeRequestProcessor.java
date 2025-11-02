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

@Component
@RequiredArgsConstructor
public class EmailChangeRequestProcessor implements VerificationProcessor {

    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;

    @Override
    public void process(VerificationToken token) {
        EmailChangeRequest emailChangeRequest = emailChangeRequestRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = emailChangeRequest.getUser();
        String newEmail = emailChangeRequest.getNewEmail();

        VerificationToken confirmationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM);
        emailChangeRequest.setVerificationToken(confirmationToken);
        emailChangeRequestRepository.save(emailChangeRequest);

        verificationEmailService.sendVerificationEmail(newEmail, confirmationToken);
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_REQUEST;
    }
}
