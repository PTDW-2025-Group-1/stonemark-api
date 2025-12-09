package pt.estga.auth.services.verification;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.user.entities.User;

import java.util.Optional;

public interface VerificationProcessingService {

    @Transactional
    Optional<String> confirmToken(String token);

    @Transactional
    Optional<String> confirmCode(String code);

    @Transactional
    void processPasswordReset(String token, String newPassword);

    void initiatePasswordReset(String contactValue);

    Optional<User> validatePasswordResetToken(String token);
}
