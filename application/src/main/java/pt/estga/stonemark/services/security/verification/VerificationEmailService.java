package pt.estga.stonemark.services.security.verification;


import pt.estga.stonemark.entities.token.VerificationToken;

public interface VerificationEmailService {
    void sendVerificationEmail(String to, VerificationToken token);
}
