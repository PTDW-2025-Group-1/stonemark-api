package pt.estga.auth.services.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
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
    private final VerificationDispatchService verificationDispatchService;
    private final PasswordEncoder passwordEncoder;
    private final UserContactRepository userContactRepository;

    @Override
    @Transactional
    public void initiatePasswordReset(String contactValue) {
        UserContact userContact = userContactRepository.findByValue(contactValue)
                .orElseThrow(() -> new UserNotFoundException("User not found with contact: " + contactValue));

        User user = userContact.getUser();
        if (!user.isEnabled()) {
            throw new UserNotFoundException("User not found with contact: " + contactValue);
        }

        if (!userContact.isVerified()) {
            throw new ContactMethodNotAvailableException("Contact is not verified: " + contactValue);
        }

        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationPurpose.PASSWORD_RESET);

        verificationDispatchService.sendVerification(userContact, verificationToken);
    }

    @Override
    public Optional<User> validatePasswordResetToken(String token) {
        return verificationTokenService.findByToken(token)
                .filter(t -> t.getPurpose() == VerificationPurpose.PASSWORD_RESET)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(java.time.Instant.now()))
                .map(VerificationToken::getUser);
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
                .filter(t -> t.getPurpose() == VerificationPurpose.PASSWORD_RESET)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiresAt().isAfter(java.time.Instant.now()))
                .orElseThrow(() -> new InvalidPasswordResetTokenException("Invalid or expired password reset token"));
    }
}
