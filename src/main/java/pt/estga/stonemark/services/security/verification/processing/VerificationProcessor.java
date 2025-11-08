package pt.estga.stonemark.services.security.verification.processing;

import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Optional;

public interface VerificationProcessor {

    Optional<String> process(VerificationToken token);

    VerificationTokenPurpose getPurpose();

}
