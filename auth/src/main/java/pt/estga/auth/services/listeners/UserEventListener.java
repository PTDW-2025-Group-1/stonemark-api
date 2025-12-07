package pt.estga.auth.services.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final VerificationInitiationService verificationInitiationService;
    private final VerificationCommandFactory verificationCommandFactory;

    @EventListener
    public void handleEmailVerificationRequested(EmailVerificationRequestedEvent event) {
        var command = verificationCommandFactory.createEmailVerificationCommand(event.getUser());
        verificationInitiationService.initiate(command);
    }

    @Async
    @EventListener
    public void handleTelephoneVerificationRequested(TelephoneVerificationRequestedEvent event) {
        var command = verificationCommandFactory.createTelephoneVerificationCommand(event.getUser());
        verificationInitiationService.initiate(command);
    }
}
