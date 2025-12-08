package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;

import java.util.Optional;

public interface UserIdentityService {

    Optional<UserIdentity> findByProviderAndIdentity(Provider provider, String identityValue);

    UserIdentity createAndAssociateUserIdentity(User user, Provider provider, String identityValue);

}
