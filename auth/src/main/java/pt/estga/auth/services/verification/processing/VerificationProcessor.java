package pt.estga.auth.services.verification.processing;

import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.Optional;

public interface VerificationProcessor {

    Optional<String> process(VerificationToken token);

    VerificationPurpose getPurpose();

}
