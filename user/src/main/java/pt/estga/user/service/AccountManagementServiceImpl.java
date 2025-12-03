package pt.estga.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import pt.estga.shared.exceptions.EmailAlreadyTakenException;
import pt.estga.user.dtos.EmailChangeRequestDto;
import pt.estga.user.dtos.TelephoneChangeRequestDto;
import pt.estga.user.dtos.TelephoneCodeVerificationDto;
import pt.estga.user.entities.User;
import pt.estga.user.events.EmailChangeRequestedEvent;
import pt.estga.user.events.TelephoneChangeRequestedEvent;

@Service
@RequiredArgsConstructor
public class AccountManagementServiceImpl implements AccountManagementService {

    private final ApplicationEventPublisher eventPublisher;
    private final UserService userService;
    private final VerificationCodeService verificationCodeService;

    @Override
    public void requestEmailChange(User user, EmailChangeRequestDto request) {
        if (userService.existsByEmail(request.newEmail())) {
            throw new EmailAlreadyTakenException("Email " + request.newEmail() + " is already taken.");
        }
        eventPublisher.publishEvent(new EmailChangeRequestedEvent(this, user, request.newEmail()));
    }

    @Override
    public void requestTelephoneChange(User user, TelephoneChangeRequestDto request) {
        eventPublisher.publishEvent(new TelephoneChangeRequestedEvent(this, user, request.newTelephone()));
    }

    @Override
    public boolean verifyTelephoneChange(User user, TelephoneCodeVerificationDto request) {

        boolean valid = verificationCodeService.validateCode(
                user,
                request.newTelephone(),
                request.code()
        );

        if (!valid) return false;

        user.setTelephone(request.newTelephone());
        userService.update(user);

        return true;
    }

}
