package pt.estga.user.services;

import pt.estga.user.entities.User;

public interface AccountManagementService {

    void requestEmailVerification(User user);

    void requestTelephoneVerification(User user);

}
