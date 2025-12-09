package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

import java.util.List;
import java.util.Optional;

public interface UserContactService {

    UserContact create(UserContact userContact);

    List<UserContact> findByUser(User user);

    Optional<UserContact> findPrimary(User user, ContactType contactType);

    Optional<UserContact> findById(Long id);

    Optional<UserContact> findByValue(String value);

    UserContact update(UserContact userContact);

    Optional<UserContact> setPrimary(UserContact userContact);

    void deleteById(Long id);

}
