package pt.estga.auth.services.verification;

import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

public interface VerificationProcessingService {

    @Transactional
    Optional<String> confirmToken(String token);

    @Transactional
    Optional<String> confirmCode(String code);

    @Transactional
    void processPasswordReset(String token, String newPassword);

    @Transactional
    void processTelephoneChange(String token, String newTelephone);

    @Transactional
    void processTelephoneChangeConfirm(String token, String code);

}
