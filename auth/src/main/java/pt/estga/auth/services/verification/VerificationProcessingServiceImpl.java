package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.processing.VerificationProcessor;
import pt.estga.auth.services.verification.processing.VerificationProcessorFactory;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.SamePasswordException;
import pt.estga.shared.exceptions.VerificationErrorMessages;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.InvalidVerificationPurposeException;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationProcessingServiceImpl implements VerificationProcessingService {

    private final VerificationTokenService verificationTokenService;
    private final VerificationProcessorFactory verificationProcessorFactory;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public Optional<String> confirmToken(String token) {
        log.info("Attempting to confirm token: {}", token);
        VerificationToken vt = getValidatedVerificationToken(token, false);
        log.debug("Token {} validated successfully. Purpose: {}", token, vt.getPurpose());

        VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
        Optional<String> resultToken = processor.process(vt);
        log.info("Token confirmation for {} completed. Password reset required: {}", token, resultToken.isPresent());
        return resultToken;
    }

    @Transactional
    @Override
    public Optional<String> confirmCode(String code) {
        log.info("Attempting to confirm code: {}", code);
        VerificationToken vt = getValidatedVerificationToken(code, true);
        log.debug("Code {} validated successfully. Purpose: {}", code, vt.getPurpose());

        VerificationProcessor processor = verificationProcessorFactory.getProcessor(vt.getPurpose());
        Optional<String> resultToken = processor.process(vt);
        log.info("Code confirmation for {} completed. Password reset required: {}", code, resultToken.isPresent());
        return resultToken;
    }

    @Transactional
    @Override
    public void processPasswordReset(String token, String newPassword) {
        log.info("Attempting to process password reset for token: {}", token);
        VerificationToken vt = getValidatedVerificationToken(token, false); // Use helper for validation
        log.debug("Token {} validated for password reset. Purpose: {}", token, vt.getPurpose());

        if (vt.getPurpose() != VerificationTokenPurpose.PASSWORD_RESET) {
            log.warn("Invalid purpose for password reset token {}. Expected PASSWORD_RESET, got {}", token, vt.getPurpose());
            throw new InvalidVerificationPurposeException(VerificationErrorMessages.INVALID_TOKEN_PURPOSE_PASSWORD_RESET);
        }

        User user = vt.getUser();
        log.debug("User associated with token {}: {}", token, user.getUsername());

        // Check if the new password is the same as the current password
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

    /**
     * Validates a verification token or code for existence, expiration, and revocation status.
     *
     * @param value The token string or code string.
     * @param isCode True if the value is a code, false if it's a token.
     * @return The validated VerificationToken.
     * @throws InvalidTokenException if the token/code is not found.
     * @throws TokenExpiredException if the token/code is expired.
     * @throws TokenRevokedException if the token/code is revoked.
     */
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
