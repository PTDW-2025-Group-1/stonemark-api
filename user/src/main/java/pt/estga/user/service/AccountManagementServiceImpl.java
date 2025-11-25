package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.user.dtos.EmailChangeRequestDto;
import pt.estga.user.entities.User;
import pt.estga.user.events.EmailChangeRequestedEvent;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;

    @Override
    public void requestEmailChange(User user, EmailChangeRequestDto request) {
        if (userService.existsByEmail(request.newEmail())) {
            throw new EmailAlreadyTakenException("Email " + request.newEmail() + " is already taken.");
        }
        eventPublisher.publishEvent(new EmailChangeRequestedEvent(this, user, request.newEmail()));
    }
}
