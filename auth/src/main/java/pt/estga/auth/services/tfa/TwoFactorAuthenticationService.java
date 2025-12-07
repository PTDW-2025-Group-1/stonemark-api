package pt.estga.auth.services.tfa;

import pt.estga.user.entities.User;

public interface TwoFactorAuthenticationService {

    String generateNewSecret();

    String generateQrCode(User user);

    boolean isCodeValid(String secret, String code);

    void enableTfa(User user);

    void disableTfa(User user);
}
