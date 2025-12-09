package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserContactService;

@Component
@RequiredArgsConstructor
public class VerificationCommandFactory {

    private final VerificationTokenService verificationTokenService;
    private final VerificationDispatchService verificationDispatchService;
    private final UserContactService userContactService;

    public VerificationCommand createEmailVerificationCommand(User user) {
        return new EmailVerificationCommand(user, verificationTokenService, verificationDispatchService, userContactService);
    }

    public VerificationCommand createTelephoneVerificationCommand(User user) {
        return new TelephoneVerificationCommand(user, verificationTokenService, verificationDispatchService, userContactService);
    }
}
