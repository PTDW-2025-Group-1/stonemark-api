package pt.estga.auth.services.passwordreset;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordResetConfirmationProcessor {

    public Optional<String> process(VerificationToken token) {
        // For password reset, we just validate the token and return it.
        // The actual password change and token revocation happens via the /reset-password endpoint.
        return Optional.of(token.getToken());
    }

    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.PASSWORD_RESET;
    }
}
