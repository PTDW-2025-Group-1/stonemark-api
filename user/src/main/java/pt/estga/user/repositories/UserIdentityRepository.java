package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;

import java.util.Optional;

@Repository
public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {

    Optional<UserIdentity> findByProviderAndValue(Provider provider, String value);

    void deleteByUserAndProvider(User user, Provider provider);

    Optional<UserIdentity> findByUserAndProvider(User user, Provider provider);

}
