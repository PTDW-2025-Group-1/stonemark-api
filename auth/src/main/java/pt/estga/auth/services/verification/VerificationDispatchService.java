package pt.estga.auth.services.verification;

import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.user.entities.UserContact;

public interface VerificationDispatchService {
    void sendVerification(UserContact userContact, VerificationToken token);
}
