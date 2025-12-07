package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.User;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void requestEmailVerification(User user) {
        eventPublisher.publishEvent(new EmailVerificationRequestedEvent(this, user));
    }

    @Override
    public void requestTelephoneVerification(User user) {
        eventPublisher.publishEvent(new TelephoneVerificationRequestedEvent(this, user));
    }
}
