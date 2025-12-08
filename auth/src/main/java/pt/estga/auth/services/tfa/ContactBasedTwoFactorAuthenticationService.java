package pt.estga.auth.services.tfa;

import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.user.entities.User;

public interface ContactBasedTwoFactorAuthenticationService {

    void generateAndSendSmsCode(User user);

    void generateAndSendEmailCode(User user);

    boolean verifyCode(User user, String code, VerificationTokenPurpose purpose);

    void requestTfaContactCode(User user);

    boolean verifyTfaContactCode(User user, String code);

}
