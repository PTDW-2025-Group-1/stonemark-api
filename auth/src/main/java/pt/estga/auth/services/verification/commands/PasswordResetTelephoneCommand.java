package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.user.entities.User;

@RequiredArgsConstructor
public class PasswordResetTelephoneCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final SmsVerificationService smsVerificationService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.PASSWORD_RESET);
        smsVerificationService.sendVerificationSms(user.getTelephone(), verificationToken);
    }
}
