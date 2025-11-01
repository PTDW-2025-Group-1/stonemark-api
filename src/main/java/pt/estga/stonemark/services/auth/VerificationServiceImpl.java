package pt.estga.stonemark.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.repositories.UserRepository;
import pt.estga.stonemark.services.EmailService;
import pt.estga.stonemark.services.token.VerificationTokenService;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private record EmailDetails(String subject, String body) {}

    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final UserRepository userRepository;

    @Value("${application.base-url}")
    private String backendBaseUrl;

    @Transactional
    @Override
    public void createAndSendToken(User user, VerificationTokenPurpose purpose) {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, purpose);

        String link = backendBaseUrl + "/api/v1/auth/confirm?token=" + verificationToken.getToken();

        EmailDetails emailDetails = composeEmailDetails(purpose, link, verificationToken);

        emailService.sendEmail(user.getEmail(), emailDetails.subject(), emailDetails.body());
    }

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
            case EMAIL_VERIFICATION:
                handleEmailVerification(vt.getUser());
                break;
            case PASSWORD_RESET:
                handlePasswordReset(vt.getUser());
                break;
            case TWO_FACTOR_AUTHENTICATION:
                handleTwoFactorAuthentication(vt.getUser());
                break;
            default:
                throw new IllegalStateException("Token has an unknown purpose: " + vt.getPurpose());
        }

        verificationTokenService.revokeToken(token);
    }

    /**
     * Enables the given user account after successful email verification.
     *
     * @param user the user whose email has been verified and should be enabled
     */
    private void handleEmailVerification(User user) {
        user.setEnabled(true);
        userRepository.save(user);
    }

    /**
     * Handles the password reset process for the given user when a password reset token is confirmed.
     *
     * @param user the user whose password is to be reset
     */
    private void handlePasswordReset(User user) {
        // TODO: Implement password reset logic.
        throw new UnsupportedOperationException("Password reset functionality is not yet implemented.");
    }

    /**
     * Handles the logic for two-factor authentication (2FA) for the given user.
     *
     * @param user The user for whom two-factor authentication is being processed.
     */
    private void handleTwoFactorAuthentication(User user) {
        // TODO: Implement 2FA logic.
        throw new UnsupportedOperationException("2FA functionality is not yet implemented.");
    }

    /**
     * Composes the subject and body of an email based on the verification token purpose.
     *
     * @param purpose The purpose of the verification token (e.g., email verification, password reset, 2FA).
     * @param link The link to be included in the email for the user to follow.
     * @param token The verification token containing expiration and token value.
     * @return An EmailDetails record containing the subject and body of the email.
     * @throws UnsupportedOperationException if the password reset functionality is not yet implemented
     */
    private EmailDetails composeEmailDetails(
            VerificationTokenPurpose purpose,
            String link,
            VerificationToken token
    ) {
        long remainingMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();

        return switch (purpose) {
            case EMAIL_VERIFICATION -> {
                long expirationHours = TimeUnit.MILLISECONDS.toHours(remainingMillis);
                yield new EmailDetails(
                        "Please verify your email",
                        String.format("""
                                Thank you for registering. Please click the link below to verify your account.
                                
                                Verification Link: %s
                                
                                If the link doesn't work, you can go to the verification page and enter the following token:
                                %s
                                
                                This token will expire in %d hours.
                                """, link, token.getToken(), expirationHours)
                );
            }
            case PASSWORD_RESET -> {
                long expirationMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
                yield new EmailDetails(
                        "Password Reset Request",
                        String.format("""
                                You requested a password reset. Please click the link below to proceed.
                                
                                Reset Link: %s
                                
                                If the link doesn't work, you can go to the password reset page and enter the following token:
                                %s
                                
                                This token will expire in %d minutes. If you did not request this, please ignore this email.
                                """, link, token.getToken(), expirationMinutes)
                );
            }
            case TWO_FACTOR_AUTHENTICATION -> {
                long expirationMinutes = TimeUnit.MILLISECONDS.toMinutes(remainingMillis);
                yield new EmailDetails(
                        "Two-Factor Authentication",
                        String.format("""
                                Your two-factor authentication link is below.

                                2FA Link: %s

                                If the link doesn't work, you can go to the 2FA page and enter the following token:
                                %s

                                This token will expire in %d minutes.
                                """, link, token.getToken(), expirationMinutes)
                );
            }
            default -> throw new IllegalArgumentException("Unsupported token purpose: " + purpose);
        };
    }
}
