package pt.estga.stonemark.services.security.verification.commands;

import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.services.security.token.VerificationTokenService;
import pt.estga.stonemark.services.security.verification.VerificationEmailService;

public class EmailVerificationCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final VerificationEmailService verificationEmailService;

    public EmailVerificationCommand(User user, VerificationTokenService verificationTokenService, VerificationEmailService verificationEmailService) {
        this.user = user;
        this.verificationTokenService = verificationTokenService;
        this.verificationEmailService = verificationEmailService;
    }

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_VERIFICATION);
        verificationEmailService.sendVerificationEmail(user.getEmail(), verificationToken);
    }
}
