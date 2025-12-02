package pt.estga.user.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import pt.estga.user.entities.User;
import pt.estga.user.events.TelephoneChangeRequestedEvent;
import pt.estga.user.service.SmsService;
import pt.estga.user.service.VerificationCodeService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelephoneChangeRequestedListener {

    private final VerificationCodeService verificationCodeService;
    private final SmsService smsService;

    @Async
    @EventListener
    public void handleTelephoneChangeRequested(TelephoneChangeRequestedEvent event) {

        User user = event.getUser();
        String newTelephone = event.getNewTelephone();
        log.info("Telephone change requested for user {} → {}", user.getEmail(), newTelephone);
        String code = verificationCodeService.generateCode(user, newTelephone);
        log.info("Generated verification code {}", code);
        smsService.sendVerificationCode(newTelephone, code);
        log.info("SMS sent to {}", newTelephone);
    }
}
