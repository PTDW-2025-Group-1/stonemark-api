package pt.estga.stonemark.services.security.verification.processing;

import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

public interface VerificationProcessor {

    void process(VerificationToken token);

    VerificationTokenPurpose getPurpose();
}
