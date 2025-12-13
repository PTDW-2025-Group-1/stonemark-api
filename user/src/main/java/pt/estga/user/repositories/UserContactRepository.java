package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long> {

    Optional<UserContact> findByValue(String value);

    Optional<UserContact> findByUserAndValue(User user, String value);

    List<UserContact> findByUser(User user);

    List<UserContact> findByUserAndTypeAndPrimaryContactAndVerified(User user, ContactType type, boolean primaryContact, boolean verified);

    boolean existsByValue(String value);

    boolean existsByValueAndVerified(String value, boolean verified);

    List<UserContact> findByValueAndVerified(String value, boolean verified);

}
