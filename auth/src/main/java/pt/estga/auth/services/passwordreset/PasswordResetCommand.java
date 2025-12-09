package pt.estga.auth.services.passwordreset;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserService;
import pt.estga.auth.services.verification.commands.VerificationCommand;

@RequiredArgsConstructor
public class PasswordResetCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationService emailVerificationService;
    private final UserContactService userContactService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationPurpose.PASSWORD_RESET);

        userContactService.findPrimary(user, ContactType.EMAIL).ifPresent(primaryEmail ->
                emailVerificationService.sendVerificationEmail(primaryEmail.getValue(), verificationToken)
        );
    }
}
