package pt.estga.verification.services.processors;

import org.springframework.stereotype.Component;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Optional;

/**
 * Processor for handling {@link ActionCodeType#RESET_PASSWORD} when confirming a code.
 * This processor simply returns the code string itself, indicating that the code is valid for password reset.
 */
@Component
public class PasswordResetTokenReturnProcessor implements VerificationPurposeProcessor {

    /**
     * Processes the action code for password reset.
     * It returns the code string wrapped in an Optional.
     *
     * @param code The {@link ActionCode} to process.
     * @return An Optional containing the code string.
     */
    @Override
    public Optional<String> process(ActionCode code) {
        return Optional.of(code.getCode());
    }

    /**
     * Returns the action code type handled by this processor.
     *
     * @return {@link ActionCodeType#RESET_PASSWORD}.
     */
    @Override
    public ActionCodeType getType() {
        return ActionCodeType.RESET_PASSWORD;
    }
}
