package pt.estga.user.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.Optional;

@Repository
public interface UserContactRepository extends JpaRepository<UserContact, Long> {

    Optional<UserContact> findByTypeAndValue(ContactType type, String value);

}
