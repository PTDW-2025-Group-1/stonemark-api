package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;

import java.util.Optional;

public interface UserIdentityService {

    UserIdentity createAndAssociate(User user, Provider provider, String identityValue);

    UserIdentity createOrUpdateTelegramIdentity(User user, String telegramId);

    Optional<UserIdentity> findByProviderAndValue(Provider provider, String value);

    Optional<UserIdentity> findByUserAndProvider(User user, Provider provider);

    void delete(UserIdentity userIdentity);

    void deleteByUserAndProvider(User user, Provider provider);

}
