package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.repositories.TelephoneChangeRequestRepository;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.VerificationEmailService;
import pt.estga.auth.repositories.EmailChangeRequestRepository;
import pt.estga.auth.services.verification.telephone.VerificationTelephoneService;
import pt.estga.user.entities.User;
import pt.estga.user.service.UserService;

@Component
@RequiredArgsConstructor
public class VerificationCommandFactory {

    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final VerificationTelephoneService verificationTelephoneService;
    private final TelephoneChangeRequestRepository telephoneChangeRequestRepository;
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

    public VerificationCommand createTelephoneVerificationCommand(User user) {
        return new TelephoneVerificationCommand(user, verificationTokenService, verificationTelephoneService);
    }

    public VerificationCommand createPasswordResetTelephoneCommand(User user) {
        return new PasswordResetTelephoneCommand(user, verificationTokenService, verificationTelephoneService);
    }

    public VerificationCommand createTelephoneChangeCommand(User user, String newTelephone) {
        return new TelephoneChangeCommand(user, newTelephone, verificationTokenService, verificationTelephoneService, telephoneChangeRequestRepository, userService);
    }
}
