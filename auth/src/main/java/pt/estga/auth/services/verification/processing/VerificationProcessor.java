package pt.estga.auth.services.verification.processing;

import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Optional;

public interface VerificationProcessor {

    Optional<String> process(VerificationToken token);

    VerificationTokenPurpose getPurpose();

}
