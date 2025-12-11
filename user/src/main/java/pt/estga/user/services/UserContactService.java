package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.List;
import java.util.Optional;

public interface UserContactService {

    UserContact create(UserContact userContact);

    List<UserContact> findAllByUser(User user);

    Optional<UserContact> findPrimary(User user, ContactType contactType);

    Optional<UserContact> findById(Long id);

    Optional<UserContact> findByValue(String value);

    Optional<UserContact> findByUserAndValue(User user, String value);

    boolean existsByValue(String value);

    boolean existsByValueAndIsVerified(String value, boolean isVerified);

    UserContact update(UserContact userContact);

    UserContact setAsPrimary(UserContact userContact);

    void delete(UserContact userContact);

    void deleteById(Long id);

}
