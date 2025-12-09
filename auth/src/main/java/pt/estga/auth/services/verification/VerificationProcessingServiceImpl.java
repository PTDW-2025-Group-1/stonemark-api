package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.shared.exceptions.*;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final GenericVerificationProcessor genericVerificationProcessor;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final UserContactRepository userContactRepository;
    private final VerificationDispatchService verificationDispatchService;

    @Transactional
    @Override
    public Optional<String> confirmToken(String token) {
        log.info("Attempting to confirm token: {}", token);
        VerificationToken vt = getValidatedVerificationToken(token, false);
        log.debug("Token {} validated successfully. Purpose: {}", token, vt.getPurpose());

        if (vt.getPurpose() == VerificationPurpose.EMAIL_VERIFICATION || vt.getPurpose() == VerificationPurpose.TELEPHONE_VERIFICATION) {
            return genericVerificationProcessor.process(vt);
        }

        if (vt.getPurpose() == VerificationPurpose.PASSWORD_RESET) {
            return Optional.of(vt.getToken());
        }

        throw new InvalidVerificationPurposeException("Invalid purpose for token confirmation");
    }

    @Transactional
    @Override
    public Optional<String> confirmCode(String code) {
        log.info("Attempting to confirm code: {}", code);
        VerificationToken vt = getValidatedVerificationToken(code, true);
        log.debug("Code {} validated successfully. Purpose: {}", code, vt.getPurpose());

        if (vt.getPurpose() == VerificationPurpose.EMAIL_VERIFICATION || vt.getPurpose() == VerificationPurpose.TELEPHONE_VERIFICATION) {
            return genericVerificationProcessor.process(vt);
        }

        throw new InvalidVerificationPurposeException("Invalid purpose for code confirmation");
    }

    @Transactional
    @Override
    public void processPasswordReset(String token, String newPassword) {
        log.info("Attempting to process password reset for token: {}", token);
        VerificationToken vt = getValidatedVerificationToken(token, false);
        log.debug("Token {} validated for password reset. Purpose: {}", token, vt.getPurpose());

        if (vt.getPurpose() != VerificationPurpose.PASSWORD_RESET) {
            log.warn("Invalid purpose for password reset token {}. Expected PASSWORD_RESET, got {}", token, vt.getPurpose());
            throw new InvalidVerificationPurposeException(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        }

        User user = vt.getUser();
        log.debug("User associated with token {}: {}", token, user.getUsername());

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            log.warn("Attempted to reset password for user {} with same password.", user.getUsername());
            throw new SamePasswordException(VerificationErrorMessages.SAME_PASSWORD);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userService.update(user);
        log.info("Password successfully reset for user {}", user.getUsername());

        verificationTokenService.revokeToken(vt);
        log.debug("Token {} revoked after successful password reset.", token);
    }

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

    private VerificationToken getValidatedVerificationToken(String value, boolean isCode) {
        log.debug("Validating {} with value: {}", isCode ? "code" : "token", value);
        Optional<VerificationToken> optionalVt;
        String notFoundMessage;
        String expiredMessage;
        String revokedMessage;

        if (isCode) {
            optionalVt = verificationTokenService.findByCode(value);
            notFoundMessage = VerificationErrorMessages.CODE_NOT_FOUND;
            expiredMessage = VerificationErrorMessages.CODE_EXPIRED;
            revokedMessage = VerificationErrorMessages.CODE_REVOKED;
        } else {
            optionalVt = verificationTokenService.findByToken(value);
            notFoundMessage = VerificationErrorMessages.TOKEN_NOT_FOUND;
            expiredMessage = VerificationErrorMessages.TOKEN_EXPIRED;
            revokedMessage = VerificationErrorMessages.TOKEN_REVOKED;
        }

        VerificationToken vt = optionalVt.orElseThrow(() -> {
            log.warn("{} not found: {}", isCode ? "Code" : "Token", value);
            return new InvalidTokenException(notFoundMessage);
        });
        log.debug("{} found. Expires at: {}, Revoked: {}", isCode ? "Code" : "Token", vt.getExpiresAt(), vt.isRevoked());


        if (vt.getExpiresAt().isBefore(Instant.now())) {
            verificationTokenService.revokeToken(vt);
            log.warn("{} {} expired. Revoked token.", isCode ? "Code" : "Token", value);
            throw new TokenExpiredException(expiredMessage);
        }

        if (vt.isRevoked()) {
            log.warn("{} {} already revoked.", isCode ? "Code" : "Token", value);
            throw new TokenRevokedException(revokedMessage);
        }
        log.debug("{} {} is valid.", isCode ? "Code" : "Token", value);
        return vt;
    }
}
