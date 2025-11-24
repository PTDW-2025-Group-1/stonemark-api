package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetConfirmationProcessor implements VerificationProcessor {

    @Override
    public Optional<String> process(VerificationToken token) {
        // For password reset, we just validate the token and return it.
        // The actual password change and token revocation happens via the /reset-password endpoint.
        return Optional.of(token.getToken());
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.PASSWORD_RESET;
    }
}
