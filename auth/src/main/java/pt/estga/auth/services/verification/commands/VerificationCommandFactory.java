package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

@Component
@RequiredArgsConstructor
public class VerificationCommandFactory {

    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationService emailVerificationService;
    private final SmsVerificationService smsVerificationService;
    private final UserService userService;

    public VerificationCommand createEmailVerificationCommand(User user) {
        return new EmailVerificationCommand(user, verificationTokenService, emailVerificationService, userService);
    }

    public VerificationCommand createTelephoneVerificationCommand(User user) {
        return new TelephoneVerificationCommand(user, verificationTokenService, smsVerificationService, userService);
    }
}
