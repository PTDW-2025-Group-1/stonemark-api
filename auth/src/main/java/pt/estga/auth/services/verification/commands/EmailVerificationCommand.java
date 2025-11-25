package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationEmailService;

@RequiredArgsConstructor
public class EmailVerificationCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_VERIFICATION);
        verificationEmailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
}
