package pt.estga.user.services;

import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;

public interface AccountService {

    void addContact(User user, String value, ContactType type);

    void requestContactVerification(User user, String value, ContactType type);

    void linkGoogleAccount(User user, String token);

    void linkTelegramAccount(User user, String token);

    void unlinkSocialAccount(User user, Provider provider);

}
