package pt.estga.auth.services.verification.commands;

import lombok.RequiredArgsConstructor;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.sms.SmsVerificationService;
import pt.estga.user.entities.User;
import pt.estga.user.services.UserService;

@RequiredArgsConstructor
public class PasswordResetTelephoneCommand implements VerificationCommand {

    private final User user;
    private final VerificationTokenService verificationTokenService;
    private final SmsVerificationService smsVerificationService;
    private final UserService userService;

    @Override
    public void execute() {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.PASSWORD_RESET);

        userService.getPrimaryTelephone(user).ifPresent(primaryTelephone ->
                smsVerificationService.sendVerificationSms(primaryTelephone, verificationToken)
        );
    }
}
