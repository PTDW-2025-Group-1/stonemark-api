package pt.estga.auth.services.tfa;

import pt.estga.auth.enums.ActionCodeType;
import pt.estga.user.entities.User;

public interface ContactBasedTwoFactorAuthenticationService {

    void generateAndSendSmsCode(User user);

    void generateAndSendEmailCode(User user);

    boolean verifyCode(User user, String code, ActionCodeType type);

    void requestTfaContactCode(User user);

    boolean verifyTfaContactCode(User user, String code);

}
