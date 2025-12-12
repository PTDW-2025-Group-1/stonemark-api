package pt.estga.user.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.shared.aop.SensitiveOperation;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.shared.exceptions.InvalidGoogleTokenException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.Provider;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserContactService userContactService;
    private final ApplicationEventPublisher eventPublisher;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserIdentityService userIdentityService;

    @Override
    public void addContact(User user, String value, ContactType type) {
        if (userContactService.existsByValueAndIsVerified(value, true)) {
            throw new IllegalArgumentException("Contact already exists and is verified.");
        }

        UserContact userContact = UserContact.builder()
                .user(user)
                .value(value)
                .type(type)
                .primaryAccount(false)
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
    public void linkGoogleAccount(User user, String token) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(token);
            if (idToken == null) {
                throw new InvalidGoogleTokenException("Invalid Google token.");
            }
            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();

            userIdentityService.createAndAssociate(user, Provider.GOOGLE, googleId);

        } catch (GeneralSecurityException | IOException e) {
            throw new InvalidGoogleTokenException("Error while verifying Google token.");
        }
    }

    @Override
    public void linkTelegramAccount(User user, String token) {
        // Todo: implement telegram account linking later
    }

    @Override
    public void unlinkSocialAccount(User user, Provider provider) {
        userIdentityService.deleteByUserAndProvider(user, Provider.GOOGLE);
    }

    @Override
    public List<UserContact> getContacts(User user) {
        return userContactService.findAllByUser(user);
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
