package pt.estga.stonemark.services.security.verification;

import org.springframework.transaction.annotation.Transactional;

public interface VerificationProcessingService {

    @Transactional
    void processTokenConfirmation(String token);

}
