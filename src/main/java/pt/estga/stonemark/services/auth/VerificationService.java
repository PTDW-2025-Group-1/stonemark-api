package pt.estga.stonemark.services.auth;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.enums.VerificationTokenPurpose;

public interface VerificationService {

    @Transactional
    void createAndSendToken(User user, VerificationTokenPurpose purpose);

    @Transactional
    void processTokenConfirmation(String token);

    void requestEmailChange(User user, String newEmail);

}
