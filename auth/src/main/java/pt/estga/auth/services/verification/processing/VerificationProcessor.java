package pt.estga.auth.services.verification.processing;

import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.Optional;

public interface VerificationProcessor {

    Optional<String> process(VerificationToken token);

    VerificationTokenPurpose getPurpose();

}
