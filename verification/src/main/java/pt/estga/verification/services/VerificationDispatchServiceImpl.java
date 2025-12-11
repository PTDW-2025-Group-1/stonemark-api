package pt.estga.verification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.user.entities.UserContact;
import pt.estga.verification.entities.ActionCode;

@Service
@RequiredArgsConstructor
@Slf4j
public class VerificationDispatchServiceImpl implements VerificationDispatchService {

    private final ActionCodeDispatchService actionCodeDispatchService;

    @Override
    public void sendVerification(UserContact userContact, ActionCode actionCode) {
        log.info("Dispatching verification for contact {} with action code id {}", userContact.getValue(), actionCode.getId());
        try {
            actionCodeDispatchService.sendVerification(userContact, actionCode);
            log.info("Successfully dispatched verification for contact {}", userContact.getValue());
        } catch (Exception e) {
            log.error("Error dispatching verification for contact {}", userContact.getValue(), e);
            throw e;
        }
    }
}
