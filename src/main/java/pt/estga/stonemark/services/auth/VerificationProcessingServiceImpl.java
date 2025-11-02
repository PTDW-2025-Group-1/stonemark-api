package pt.estga.stonemark.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.services.email.EmailService;
import pt.estga.stonemark.services.token.VerificationTokenService;

import java.time.Instant;

@Service("verificationProcessingServiceImpl")
@RequiredArgsConstructor
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final VerificationInitiationService verificationInitiationService;

    @Transactional
    @Override
    public void processTokenConfirmation(String token) {
        VerificationToken vt = verificationTokenService.findByToken(token)
                .orElseThrow(() -> new InvalidTokenException("Token not found."));

        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(token);
            throw new InvalidTokenException("Token has expired.");
        }

        switch (vt.getPurpose()) {
            case EMAIL_VERIFICATION -> handleEmailVerification(vt);
            case PASSWORD_RESET -> handlePasswordReset(vt);
            case TWO_FACTOR_AUTHENTICATION -> handleTwoFactorAuthentication(vt);
            case EMAIL_CHANGE_REQUEST -> handleEmailChangeRequest(vt);
            case EMAIL_CHANGE_CONFIRM -> handleEmailChangeConfirm(vt);
            default -> throw new IllegalStateException("Token has an unknown purpose: " + vt.getPurpose());
        }

        verificationTokenService.revokeToken(token);
    }

    private void handleEmailVerification(VerificationToken verificationToken) {
        User user = verificationToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
    }

    private void handlePasswordReset(VerificationToken verificationToken) {
        // TODO: Implement password reset logic.
        throw new UnsupportedOperationException("Password reset functionality is not yet implemented.");
    }

    private void handleTwoFactorAuthentication(VerificationToken verificationToken) {
        // TODO: Implement 2FA logic.
        throw new UnsupportedOperationException("2FA functionality is not yet implemented.");
    }

    private void handleEmailChangeRequest(VerificationToken verificationToken) {
        EmailChangeRequest emailChangeRequest = emailChangeRequestRepository.findByVerificationToken(verificationToken)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = emailChangeRequest.getUser();
        String newEmail = emailChangeRequest.getNewEmail();

        VerificationToken confirmationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM);
        emailChangeRequest.setVerificationToken(confirmationToken);
        emailChangeRequestRepository.save(emailChangeRequest);

        // Send the confirmation email to the new email address
        verificationInitiationService.sendEmailChangeConfirmation(newEmail, confirmationToken);
    }

    private void handleEmailChangeConfirm(VerificationToken verificationToken) {
        EmailChangeRequest emailChangeRequest = emailChangeRequestRepository.findByVerificationToken(verificationToken)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = emailChangeRequest.getUser();
        String newEmail = emailChangeRequest.getNewEmail();

        emailService.sendEmail(Email.builder()
                .to(user.getEmail())
                .subject("Email Address Changed")
                .template("email/email-changed-notification.html")
                .build());

        user.setEmail(newEmail);
        user.setGoogleId(null);
        userRepository.save(user);

        emailChangeRequestRepository.delete(emailChangeRequest);
    }
}
