package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.auth.services.verification.commands.PasswordResetInitiationCommand;
import pt.estga.auth.services.verification.commands.VerificationCommand;

/**
 * Service responsible for initiating various verification flows.
 * It acts as an invoker for different {@link VerificationCommand}s,
 * delegating the specific initiation logic to the commands themselves.
 */
@Service
@RequiredArgsConstructor
public class VerificationInitiationServiceImpl implements VerificationInitiationService {

    private final PasswordResetInitiationCommand passwordResetInitiationCommand; // Injected

    /**
     * Initiates a verification process by executing the given command.
     * This method serves as a generic entry point for all verification initiation types.
     *
     * @param command The {@link VerificationCommand} to be executed.
     */
    @Override
    public void initiate(VerificationCommand<Void> command) {
        command.execute(null);
    }

    /**
     * Initiates a password reset verification for a given contact value.
     * This method now uses the injected {@link PasswordResetInitiationCommand}.
     *
     * @param contactValue The contact value (e.g., email or phone number) for which to initiate password reset.
     */
    @Override
    public void initiatePasswordReset(String contactValue) {
        passwordResetInitiationCommand.execute(contactValue);
    }
}
