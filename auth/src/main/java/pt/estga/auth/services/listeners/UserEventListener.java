package pt.estga.auth.services.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.user.entities.User;
import pt.estga.user.events.EmailChangeRequestedEvent;
import pt.estga.user.events.TelephoneChangeRequestedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final VerificationInitiationService verificationInitiationService;
    private final VerificationCommandFactory verificationCommandFactory;

    @EventListener
    public void handleEmailChangeRequested(EmailChangeRequestedEvent event) {
        var command = verificationCommandFactory.createEmailChangeCommand(event.getUser(), event.getNewEmail());
        verificationInitiationService.initiate(command);
    }

    @Async
    @EventListener
    public void handleTelephoneChangeRequested(TelephoneChangeRequestedEvent event) {
        var command = verificationCommandFactory.createTelephoneChangeCommand(event.getUser(), event.getNewTelephone());
        verificationInitiationService.initiate(command);
    }
}
