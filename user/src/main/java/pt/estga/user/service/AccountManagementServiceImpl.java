package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pt.estga.user.dtos.EmailChangeRequestDto;
import pt.estga.user.dtos.TelephoneChangeRequestDto;
import pt.estga.user.entities.User;
import pt.estga.user.events.EmailChangeRequestedEvent;
import pt.estga.user.events.TelephoneChangeRequestedEvent;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    @Override
    public void requestEmailChange(User user, EmailChangeRequestDto request) {
        eventPublisher.publishEvent(new EmailChangeRequestedEvent(this, user, request.newEmail()));
    }

    @Override
    public void requestTelephoneChange(User user, TelephoneChangeRequestDto request) {
        eventPublisher.publishEvent(new TelephoneChangeRequestedEvent(this, user, request.newTelephone()));
    }
}
