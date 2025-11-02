package pt.estga.stonemark.services.security.token;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.repositories.token.VerificationTokenRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerificationTokenServiceImpl implements VerificationTokenService {

    private final VerificationTokenRepository verificationTokenRepository;

    @Value("${application.security.jwt.email-verification-token.expiration}")
    private long emailVerificationTokenExpiration;

    @Value("${application.security.jwt.password-reset-token.expiration}")
    private long passwordResetTokenExpiration;

    @Value("${application.security.jwt.two-factor-authentication-token.expiration}")
    private long twoFactorAuthenticationTokenExpiration;

    @Value("${application.security.jwt.email-change-request-token.expiration}")
    private long emailChangeRequestTokenExpiration;

    @Value("${application.security.jwt.email-change-confirm-token.expiration}")
    private long emailChangeConfirmTokenExpiration;

    @Override
    public VerificationToken createAndSaveToken(User user, VerificationTokenPurpose purpose) {
        long expirationMillis = getExpirationMillisFor(purpose);
        String token = UUID.randomUUID().toString();

        VerificationToken vt = VerificationToken.builder()
                .token(token)
                .user(user)
                .purpose(purpose)
                .expiresAt(Instant.now().plusMillis(expirationMillis))
                .build();

        return verificationTokenRepository.save(vt);
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return verificationTokenRepository.findByToken(token);
    }

    @Override
    public boolean isTokenValid(String token) {
        return findByToken(token)
                .map(t -> !t.isRevoked() && t.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public void revokeToken(String token) {
        findByToken(token).ifPresent(t -> {
            t.setRevoked(true);
            verificationTokenRepository.save(t);
        });
    }

    private long getExpirationMillisFor(VerificationTokenPurpose purpose) {
        return switch (purpose) {
            case EMAIL_VERIFICATION -> emailVerificationTokenExpiration;
            case PASSWORD_RESET -> passwordResetTokenExpiration;
            case TWO_FACTOR_AUTHENTICATION -> twoFactorAuthenticationTokenExpiration;
            case EMAIL_CHANGE_REQUEST -> emailChangeRequestTokenExpiration;
            case EMAIL_CHANGE_CONFIRM -> emailChangeConfirmTokenExpiration;
        };
    }
}
