package pt.estga.auth.services.tfa;

import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.verification.enums.ActionCodeType;

public interface TwoFactorAuthenticationService {

    void generateAndSendSmsCode(User user);

    void generateAndSendEmailCode(User user);

    boolean verifyCode(User user, String code, ActionCodeType type);

    void requestTfaContactCode(User user);

    boolean verifyTfaContactCode(User user, String code);

    void setTfaMethod(User user, TfaMethod method);

}
