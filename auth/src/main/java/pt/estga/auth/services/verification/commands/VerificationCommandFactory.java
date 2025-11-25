package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.services.user.UserService;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationEmailService;

@Component
@RequiredArgsConstructor
public class VerificationCommandFactory {

    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserService userService;

    public VerificationCommand createEmailVerificationCommand(User user) {
        return new EmailVerificationCommand(user, verificationTokenService, verificationEmailService);
    }

    public VerificationCommand createPasswordResetCommand(User user) {
        return new PasswordResetCommand(user, verificationTokenService, verificationEmailService);
    }

    public VerificationCommand createEmailChangeCommand(User user, String newEmail) {
        return new EmailChangeCommand(user, newEmail, verificationTokenService, verificationEmailService, emailChangeRequestRepository, userService);
    }
}
