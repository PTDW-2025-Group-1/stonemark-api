package pt.estga.auth.services.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.verification.processing.VerificationProcessor;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetConfirmationProcessor implements VerificationProcessor {

    public Optional<String> process(VerificationToken token) {
        // For password reset, we just validate the token and return it.
        // The actual password change and token revocation happens via the /reset-password endpoint.
        return Optional.of(token.getToken());
    }

    public VerificationPurpose getPurpose() {
        return VerificationPurpose.PASSWORD_RESET;
    }
}
