package pt.estga.stonemark.services.security.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.repositories.PasswordResetRequestRepository;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.VerificationEmailService;

@Component
@RequiredArgsConstructor
public class VerificationCommandFactory {

    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final PasswordResetRequestRepository passwordResetRequestRepository;
    private final UserService userService;

    public VerificationCommand createEmailVerificationCommand(User user) {
        return new EmailVerificationCommand(user, verificationTokenService, verificationEmailService);
    }

    public VerificationCommand createPasswordResetCommand(User user, String newPassword) {
        return new PasswordResetCommand(user, newPassword, verificationTokenService, verificationEmailService, passwordResetRequestRepository);
    }

    public VerificationCommand createEmailChangeCommand(User user, String newEmail) {
        return new EmailChangeCommand(user, newEmail, verificationTokenService, verificationEmailService, emailChangeRequestRepository, userService);
    }
}
