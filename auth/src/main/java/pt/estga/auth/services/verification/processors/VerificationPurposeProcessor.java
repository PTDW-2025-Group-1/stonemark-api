package pt.estga.auth.services.verification.processors;

import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;

import java.util.Optional;

/**
 * Interface for processing action codes based on their type.
 * This is part of a Strategy pattern to handle different verification outcomes.
 */
public interface VerificationPurposeProcessor {

    /**
     * Processes the given action code.
     * The implementation will define the specific logic for a particular action code type.
     *
     * @param code The {@link ActionCode} to be processed.
     * @return An {@link Optional<String>} which might contain a value (e.g., the code itself for password reset)
     *         or be empty if no specific value needs to be returned.
     */
    Optional<String> process(ActionCode code);

    /**
     * Returns the {@link ActionCodeType} that this processor handles.
     *
     * @return The {@link ActionCodeType} enum value.
     */
    ActionCodeType getType();
}
