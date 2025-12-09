package pt.estga.user.services;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pt.estga.shared.exceptions.ContactMethodNotAvailableException;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserContactService userContactService;

    @Override
    public void requestEmailVerification(User user) {
        UserContact primaryEmailContact = userContactService.findPrimary(user, ContactType.EMAIL)
                .orElseThrow(() -> new ContactMethodNotAvailableException("Primary email contact not found for user " + user.getUsername()));
        eventPublisher.publishEvent(new EmailVerificationRequestedEvent(this, user, primaryEmailContact));
    }

    @Override
    public void requestTelephoneVerification(User user) {
        UserContact primaryTelephoneContact = userContactService.findPrimary(user, ContactType.TELEPHONE)
                .orElseThrow(() -> new ContactMethodNotAvailableException("Primary telephone contact not found for user " + user.getUsername()));
        eventPublisher.publishEvent(new TelephoneVerificationRequestedEvent(this, user, primaryTelephoneContact));
    }
}
