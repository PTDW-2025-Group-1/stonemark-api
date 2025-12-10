package pt.estga.auth.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.services.ActionCodeService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.ActionCodeCommand;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final VerificationInitiationService verificationInitiationService;
    private final ActionCodeService actionCodeService;
    private final VerificationDispatchService verificationDispatchService;

    @EventListener
    public void handleEmailVerificationRequested(EmailVerificationRequestedEvent event) {
        var command = new ActionCodeCommand(event.getUser(), event.getUserContact(), actionCodeService, verificationDispatchService, ActionCodeType.EMAIL_VERIFICATION);
        verificationInitiationService.initiate(command);
    }

    @Async
    @EventListener
    public void handleTelephoneVerificationRequested(TelephoneVerificationRequestedEvent event) {
        var command = new ActionCodeCommand(event.getUser(), event.getUserContact(), actionCodeService, verificationDispatchService, ActionCodeType.PHONE_VERIFICATION);
        verificationInitiationService.initiate(command);
    }
}
