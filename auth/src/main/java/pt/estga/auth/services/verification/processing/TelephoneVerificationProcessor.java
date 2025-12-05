package pt.estga.auth.services.verification.processing;

import org.springframework.stereotype.Component;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.Optional;

@Component
public class TelephoneVerificationProcessor implements VerificationProcessor {

    @Override
    public Optional<String> process(VerificationToken token) {
        // TODO: Implement telephone verification logic
        return Optional.empty();
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TELEPHONE_VERIFICATION;
    }
}
