package pt.estga.auth.services.tfa;

import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;

public interface TwoFactorAuthenticationService {

    String generateNewSecret();

    String generateQrCode(User user);

    boolean isCodeValid(String secret, String code);

    void enableTfa(User user, TfaMethod method);

    void disableTfa(User user);
}
