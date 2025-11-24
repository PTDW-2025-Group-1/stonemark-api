package pt.estga.stonemark.services.security.verification;

import pt.estga.stonemark.services.security.verification.commands.VerificationCommand;

public interface VerificationInitiationService {
    void initiate(VerificationCommand command);
}
