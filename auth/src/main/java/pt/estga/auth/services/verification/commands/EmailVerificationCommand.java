package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.email.EmailVerificationService;
import pt.estga.user.entities.User;

@RequiredArgsConstructor
public class EmailVerificationCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final EmailVerificationService emailVerificationService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_VERIFICATION);
        emailVerificationService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
}
