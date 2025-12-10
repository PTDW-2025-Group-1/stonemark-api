package pt.estga.auth.services.verification;

import pt.estga.auth.services.verification.commands.VerificationCommand;

public interface VerificationInitiationService {

    void initiate(VerificationCommand<Void> command);

    void initiatePasswordReset(String contactValue);

}
