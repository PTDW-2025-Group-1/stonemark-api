package pt.estga.auth.services.verification;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.dtos.auth.ConfirmationResponseDto;

public interface VerificationProcessingService {

    @Transactional
    ConfirmationResponseDto processTokenConfirmation(String token);

    @Transactional
    ConfirmationResponseDto processCodeConfirmation(String code);

    @Transactional
    void processPasswordReset(String token, String newPassword);

}
