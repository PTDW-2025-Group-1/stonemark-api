package pt.estga.stonemark.services.security.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.request.PasswordResetRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.repositories.PasswordResetRequestRepository;
import pt.estga.stonemark.services.PasswordService;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.email.EmailService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;

import java.time.Instant;

@Service("verificationProcessingServiceImpl")
@RequiredArgsConstructor
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final UserService userService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final VerificationInitiationService verificationInitiationService;
    private final PasswordService passwordService;

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
        userService.update(user);
    }

    private void handlePasswordReset(VerificationToken verificationToken) {
        PasswordResetRequest passwordResetRequest = passwordResetRequestRepository.findByVerificationToken(verificationToken)
                .orElseThrow(() -> new InvalidTokenException("Password reset request not found."));

        User user = passwordResetRequest.getUser();
        passwordService.resetPassword(user, passwordResetRequest);

        passwordResetRequestRepository.delete(passwordResetRequest);
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
        userService.update(user);

        emailChangeRequestRepository.delete(emailChangeRequest);
    }
}
