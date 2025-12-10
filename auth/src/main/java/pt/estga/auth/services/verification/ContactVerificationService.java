package pt.estga.auth.services.verification;

import pt.estga.auth.entities.ActionCode;
import pt.estga.user.enums.ContactType;

/**
 * A service for sending a verification code to a specific type of contact.
 */
public interface ContactVerificationService {

    /**
     * Returns the contact type that this service can handle.
     *
     * @return The contact type.
     */
    ContactType getContactType();

    /**
     * Sends a verification code to the given contact.
     *
     * @param contactValue The value of the contact (e.g., email address, phone number).
     * @param code        The verification code to send.
     */
    void sendVerification(String contactValue, ActionCode code);
}
