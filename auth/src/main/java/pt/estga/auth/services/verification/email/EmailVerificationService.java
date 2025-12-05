package pt.estga.auth.services.verification.email;

import pt.estga.auth.entities.token.VerificationToken;

public interface EmailVerificationService {

    void sendVerificationEmail(String to, VerificationToken token);

}
