package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;

import java.util.List;
import java.util.UUID;

public interface AccountService {

    void addContact(User user, String value, ContactType type);

    void requestContactVerification(User user, String value, ContactType type);

    void linkGoogleAccount(User user, String token);

    void linkTelegramAccount(User user, String token);

    void unlinkSocialAccount(User user, Provider provider);

    List<UserContact> getContacts(User user);

    void deleteContact(User user, Long contactId, String passwordOrTfaCode);

}
