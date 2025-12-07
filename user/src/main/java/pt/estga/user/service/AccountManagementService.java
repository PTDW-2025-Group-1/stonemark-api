package pt.estga.user.service;

import pt.estga.user.entities.User;

public interface AccountManagementService {

    void requestEmailVerification(User user);

    void requestTelephoneVerification(User user);

}
