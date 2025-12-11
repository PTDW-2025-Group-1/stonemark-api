package pt.estga.verification.services.processors;

import pt.estga.user.entities.UserContact;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Optional;

/**
 * Interface for processing action codes based on their type.
 * This is part of a Strategy pattern to handle different verification outcomes.
 */
public interface VerificationProcessor {

    /**
     * Processes the given action code.
     * The implementation will define the specific logic for a particular action code type.
     *
     * @param userContact The {@link UserContact} associated with the verification. Can be null if not applicable.
     * @param code The {@link ActionCode} to be processed.
     * @return An {@link Optional<String>} which might contain a value (e.g., the code itself for password reset)
     *         or be empty if no specific value needs to be returned.
     */
    Optional<String> process(UserContact userContact, ActionCode code);

    /**
     * Returns the {@link ActionCodeType} that this processor handles.
     *
     * @return The {@link ActionCodeType} enum value.
     */
    ActionCodeType getType();
}
