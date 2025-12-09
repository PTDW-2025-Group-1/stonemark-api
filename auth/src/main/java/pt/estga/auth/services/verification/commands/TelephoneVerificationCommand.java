package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;

@RequiredArgsConstructor
public class TelephoneVerificationCommand implements VerificationCommand {

    private final User user;
    private final UserContact userContact;
    private final VerificationTokenService verificationTokenService;
    private final VerificationDispatchService verificationDispatchService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationPurpose.TELEPHONE_VERIFICATION);
        verificationDispatchService.sendVerification(userContact, verificationToken);
    }
}
