package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.shared.aop.SensitiveOperation;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.user.dtos.AccountSecurityStatusDto;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserContactService userContactService;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void addContact(User user, String value, ContactType type) {
        if (userContactService.existsByValueAndIsVerified(value, true)) {
            throw new IllegalArgumentException("Contact already exists and is verified.");
        }

        UserContact userContact = UserContact.builder()
                .user(user)
                .value(value)
                .type(type)
                .primaryContact(false)
                .verified(false)
                .build();
        userContactService.create(userContact);
    }

    @Override
    public void requestContactVerification(User user, Long contactId) {
        UserContact contact = userContactService.findById(contactId)
                .orElseThrow(() -> new ContactMethodNotAvailableException("Contact not found."));
        if (!contact.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Contact does not belong to user.");
        }
        if (contact.isVerified()) {
            throw new IllegalArgumentException("Contact already verified.");
        }
        ContactType type = contact.getType();
        switch (type) {
            case EMAIL:
                log.info("Requesting verification for email: {}", contact.getValue());
                eventPublisher.publishEvent(new EmailVerificationRequestedEvent(this, user, contact));
                break;
            case TELEPHONE:
                log.info("Requesting verification for telephone: {}", contact.getValue());
                eventPublisher.publishEvent(new TelephoneVerificationRequestedEvent(this, user, contact));
                break;
            default:
                log.error("Unsupported contact type for verification: {}", type);
                throw new IllegalArgumentException("Unsupported contact type for verification: " + type);
        }
    }

    @Override
    public List<UserContact> getContacts(User user) {
        return userContactService.findAllByUser(user);
    }

    @Override
    @Transactional
    public AccountSecurityStatusDto getSecurityStatus(User user) {

        User managedUser = userService
                .findById(user.getId())
                .orElseThrow();

        boolean hasPassword =
                managedUser.getPassword() != null &&
                !managedUser.getPassword().isBlank();

        return new AccountSecurityStatusDto(hasPassword);
    }

    @Override
    @Transactional
    public void setPrimaryContact(User user, Long contactId) {

        UserContact contact = userContactService.findById(contactId)
                .orElseThrow(() -> new IllegalArgumentException("Contact not found."));

        if (!contact.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Contact does not belong to user.");
        }

        if (!contact.isVerified()) {
            throw new IllegalStateException("Contact must be verified before being set as primary.");
        }

        userContactService.setAsPrimary(contact);
    }

    @Override
    @SensitiveOperation(reason = "delete_contact")
    public void deleteContact(User user, Long contactId) {
        UserContact contact = userContactService.findById(contactId)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Contact not found for user."));
        userContactService.delete(contact);
    }
}
