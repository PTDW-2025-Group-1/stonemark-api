package pt.estga.auth.services.verification;

import pt.estga.auth.entities.token.VerificationToken;

public interface VerificationEmailService {
    void sendVerificationEmail(String to, VerificationToken token);
}
