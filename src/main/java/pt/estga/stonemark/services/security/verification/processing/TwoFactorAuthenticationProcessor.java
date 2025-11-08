package pt.estga.stonemark.services.security.verification.processing;

import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Optional;

@Component
public class TwoFactorAuthenticationProcessor implements VerificationProcessor {

    @Override
    public Optional<String> process(VerificationToken token) {
        // TODO: Implement 2FA logic.
        throw new UnsupportedOperationException("2FA functionality is not yet implemented.");
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TWO_FACTOR_AUTHENTICATION;
    }
}
