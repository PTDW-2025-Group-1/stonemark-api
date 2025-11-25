package pt.estga.auth.services.verification;

import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.dtos.ConfirmationResponseDto;

public interface VerificationProcessingService {

    // Todo change signature to get reed of dtos
    @Transactional
    ConfirmationResponseDto processTokenConfirmation(String token);

    @Transactional
    ConfirmationResponseDto processCodeConfirmation(String code);

    @Transactional
    void processPasswordReset(String token, String newPassword);

}
