package pt.estga.stonemark.services.auth;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;

public interface VerificationService {

    @Transactional
    String createAndSendToken(User user);

    @Transactional
    boolean confirmToken(String token);

}
