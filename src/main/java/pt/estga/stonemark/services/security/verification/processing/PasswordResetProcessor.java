package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.PasswordResetRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.repositories.PasswordResetRequestRepository;
import pt.estga.stonemark.services.PasswordService;

@Component
@RequiredArgsConstructor
public class PasswordResetProcessor implements VerificationProcessor {

    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final PasswordService passwordService;

    @Override
    public void process(VerificationToken token) {
        PasswordResetRequest passwordResetRequest = passwordResetRequestRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Password reset request not found."));

        User user = passwordResetRequest.getUser();
        passwordService.resetPassword(user, passwordResetRequest);

        passwordResetRequestRepository.delete(passwordResetRequest);
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.PASSWORD_RESET;
    }
}
