package pt.estga.auth.services.tfa;

import pt.estga.auth.dtos.TfaSetupResponseDto;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;

public interface TotpService {

    boolean isCodeValid(String secret, String code);

    void enableTfa(User user, TfaMethod method);

    void disableTfa(User user);

    TfaSetupResponseDto setupTotpForUser(User user);

    boolean verifyAndDisableTfa(User user, String code);

}
