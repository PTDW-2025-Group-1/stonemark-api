package pt.estga.stonemark.services.auth;

import org.springframework.transaction.annotation.Transactional;

public interface VerificationProcessingService {

    @Transactional
    void processTokenConfirmation(String token);

}
