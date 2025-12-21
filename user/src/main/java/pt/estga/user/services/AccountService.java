package pt.estga.user.services;

import pt.estga.user.dtos.AccountSecurityStatusDto;
import pt.estga.user.dtos.LinkedProviderDto;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;

import java.util.List;

public interface AccountService {

    void addContact(User user, String value, ContactType type);

    void requestContactVerification(User user, Long contactId);

    List<UserContact> getContacts(User user);

    AccountSecurityStatusDto getSecurityStatus(User user);

    void setPrimaryContact(User user, Long contactId);

    void deleteContact(User user, Long contactId);

}
