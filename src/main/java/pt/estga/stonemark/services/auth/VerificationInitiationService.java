package pt.estga.stonemark.services.auth;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

public interface VerificationInitiationService {

    @Transactional
    void createAndSendToken(User user, VerificationTokenPurpose purpose);

    void requestEmailChange(User user, String newEmail);

    void sendEmailChangeConfirmation(String newEmail, VerificationToken confirmationToken);

}
