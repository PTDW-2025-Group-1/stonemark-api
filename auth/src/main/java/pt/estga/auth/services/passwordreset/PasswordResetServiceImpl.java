package pt.estga.auth.services.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.user.services.UserService;
import pt.estga.shared.exceptions.UserNotFoundException;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.InvalidPasswordResetTokenException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserService userService;
    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationService emailVerificationService;
    private final SmsVerificationService smsVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final UserContactRepository userContactRepository;

    @Override
    @Transactional
    public void initiatePasswordReset(String contactValue) {
        UserContact userContact = userContactRepository.findByValue(contactValue)
                .orElseThrow(() -> new UserNotFoundException("User not found with contact: " + contactValue));

        if (!userContact.isVerified()) {
            throw new ContactMethodNotAvailableException("Contact is not verified: " + contactValue);
        }

        User user = userContact.getUser();
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.PASSWORD_RESET);

        if (userContact.getType() == ContactType.EMAIL) {
            emailVerificationService.sendVerificationEmail(userContact.getValue(), verificationToken);
        } else if (userContact.getType() == ContactType.TELEPHONE) {
            smsVerificationService.sendVerificationSms(userContact.getValue(), verificationToken);
        } else {
            throw new IllegalArgumentException("Unsupported contact type for password reset: " + userContact.getType());
        }
    }

    @Override
    public Optional<User> validatePasswordResetToken(String token) {
        try {
            VerificationToken verificationToken = getValidPasswordResetToken(token);
            return Optional.of(verificationToken.getUser());
        } catch (InvalidPasswordResetTokenException e) {
            // If the token is invalid, expired, or used, we return empty optional
            // The exception is caught here to allow the caller to handle it gracefully
            return Optional.empty();
        }
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        VerificationToken verificationToken = getValidPasswordResetToken(token);

        User user = verificationToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);

        verificationTokenService.revokeToken(verificationToken);
    }

    /**
     * Helper method to retrieve and validate a password reset token.
     *
     * @param token The token string.
     * @return The valid VerificationToken.
     * @throws InvalidPasswordResetTokenException if the token is invalid, expired, or already used.
     */
    private VerificationToken getValidPasswordResetToken(String token) {
        return verificationTokenService.findByToken(token)
                .filter(t -> t.getPurpose() == VerificationTokenPurpose.PASSWORD_RESET)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(java.time.Instant.now()))
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired password reset token"));
    }
}
