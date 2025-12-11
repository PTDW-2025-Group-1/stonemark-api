package pt.estga.user.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
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

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final UserContactService userContactService;
    private final ApplicationEventPublisher eventPublisher;
    private final GoogleIdTokenVerifier googleIdTokenVerifier;
    private final UserIdentityService userIdentityService;

    @Override
    public void addContact(User user, String value, ContactType type) {
        UserContact userContact = UserContact.builder()
                .user(user)
                .value(value)
                .type(type)
                .isPrimary(false)
                .isVerified(false)
                .build();
        userContactService.create(userContact);
    }

    @Override
    public void requestContactVerification(User user, String value, ContactType type) {
        UserContact contact = userContactService.findByValue(value)
                .filter(c -> c.getUser().equals(user) && c.getType() == type)
                .orElseThrow(() -> new ContactMethodNotAvailableException("Contact not found for user."));

        switch (type) {
            case EMAIL -> eventPublisher.publishEvent(new EmailVerificationRequestedEvent(this, user, contact));
            case TELEPHONE ->
                    eventPublisher.publishEvent(new TelephoneVerificationRequestedEvent(this, user, contact));
            default -> throw new IllegalArgumentException("Unsupported contact type: " + type);
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
    public void deleteContact(User user, Long contactId, String passwordOrTfaCode) {
        UserContact contact = userContactService.findById(contactId)
                .filter(c -> c.getUser().equals(user))
                .orElseThrow(() -> new IllegalArgumentException("Contact not found for user."));
        userContactService.delete(contact);
    }
}
