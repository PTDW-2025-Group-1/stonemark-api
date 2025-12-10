package pt.estga.verification.services.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.services.UserActivationService;

import java.util.Optional;

/**
 * Processor for handling {@link ActionCodeType#EMAIL_VERIFICATION}.
 * This component delegates the actual user enabling logic to {@link UserActivationService}.
 */
@Component
@RequiredArgsConstructor
public class EmailVerificationPurposeProcessor implements VerificationPurposeProcessor {

    private final UserActivationService userActivationService;

    /**
     * Processes the action code for email verification.
     * It enables the associated user and consumes the code.
     *
     * @param code The {@link ActionCode} to process.
     * @return An empty Optional, as enabling a user typically doesn't return a string.
     */
    @Override
    public Optional<String> process(ActionCode code) {
        return userActivationService.activateUserAndConsumeCode(code);
    }

    /**
     * Returns the action code type handled by this processor.
     *
     * @return {@link ActionCodeType#EMAIL_VERIFICATION}.
     */
    @Override
    public ActionCodeType getType() {
        return ActionCodeType.EMAIL_VERIFICATION;
    }
}
