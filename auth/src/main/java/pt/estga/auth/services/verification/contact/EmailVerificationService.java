package pt.estga.auth.services.verification.contact;

import pt.estga.auth.entities.token.VerificationToken;

public interface EmailVerificationService {

    void sendVerificationEmail(String to, VerificationToken token);

}
