package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.telephone.VerificationTelephoneService;
import pt.estga.user.entities.User;

@RequiredArgsConstructor
public class TelephoneVerificationCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final VerificationTelephoneService verificationTelephoneService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.TELEPHONE_VERIFICATION);
        verificationTelephoneService.sendVerificationSms(user.getTelephone(), verificationToken);
    }
}
