package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.services.UserContactService;

@RequiredArgsConstructor
public class EmailVerificationCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final VerificationDispatchService verificationDispatchService;
    private final UserContactService contactService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationPurpose.EMAIL_VERIFICATION);

        contactService.findPrimary(user, ContactType.EMAIL).ifPresent(primaryEmail ->
                verificationDispatchService.sendVerification(primaryEmail, verificationToken)
        );
    }
}
