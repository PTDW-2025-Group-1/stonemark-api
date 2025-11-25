package pt.estga.auth.services.listeners;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.VerificationCommandFactory;
import pt.estga.user.events.EmailChangeRequestedEvent;

@Service
@RequiredArgsConstructor
public class UserEventListener {

    private final VerificationInitiationService verificationInitiationService;
    private final VerificationCommandFactory verificationCommandFactory;

    @EventListener
    public void handleEmailChangeRequested(EmailChangeRequestedEvent event) {
        var command = verificationCommandFactory.createEmailChangeCommand(event.getUser(), event.getNewEmail());
        verificationInitiationService.initiate(command);
    }
}
