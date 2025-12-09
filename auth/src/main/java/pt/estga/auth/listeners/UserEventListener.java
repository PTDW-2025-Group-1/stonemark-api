package pt.estga.auth.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import pt.estga.auth.services.token.VerificationTokenService;
import pt.estga.auth.services.verification.VerificationDispatchService;
import pt.estga.auth.services.verification.VerificationInitiationService;
import pt.estga.auth.services.verification.commands.EmailVerificationCommand;
import pt.estga.auth.services.verification.commands.TelephoneVerificationCommand;
import pt.estga.user.events.EmailVerificationRequestedEvent;
import pt.estga.user.events.TelephoneVerificationRequestedEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final VerificationInitiationService verificationInitiationService;
    private final VerificationTokenService verificationTokenService;
    private final VerificationDispatchService verificationDispatchService;

    @EventListener
    public void handleEmailVerificationRequested(EmailVerificationRequestedEvent event) {
        var command = new EmailVerificationCommand(event.getUser(), event.getUserContact(), verificationTokenService, verificationDispatchService);
        verificationInitiationService.initiate(command);
    }

    @Async
    @EventListener
    public void handleTelephoneVerificationRequested(TelephoneVerificationRequestedEvent event) {
        var command = new TelephoneVerificationCommand(event.getUser(), event.getUserContact(), verificationTokenService, verificationDispatchService);
        verificationInitiationService.initiate(command);
    }
}
