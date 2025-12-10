package pt.estga.verification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pt.estga.verification.entities.ActionCode;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.exceptions.TokenExpiredException;
import pt.estga.shared.exceptions.TokenRevokedException;
import pt.estga.shared.exceptions.VerificationErrorMessages;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ActionCodeValidationServiceImpl implements ActionCodeValidationService {

    private final ActionCodeService actionCodeService;

    @Override
    public ActionCode getValidatedActionCode(String value) {
        log.debug("Validating code with value: {}", value);
        Optional<ActionCode> optionalAc = actionCodeService.findByCode(value);

        ActionCode ac = optionalAc.orElseThrow(() -> {
            log.warn("Code not found: {}", value);
            return new InvalidTokenException(VerificationErrorMessages.CODE_NOT_FOUND);
        });
        log.debug("Code found. Expires at: {}, Consumed: {}", ac.getExpiresAt(), ac.isConsumed());


        if (ac.getExpiresAt().isBefore(Instant.now())) {
            actionCodeService.consumeCode(ac);
            log.warn("Code {} expired. Consumed code.", value);
            throw new TokenExpiredException(VerificationErrorMessages.CODE_EXPIRED);
        }

        if (ac.isConsumed()) {
            log.warn("Code {} already consumed.", value);
            throw new TokenRevokedException(VerificationErrorMessages.CODE_REVOKED);
        }
        log.debug("Code {} is valid.", value);
        return ac;
    }
}
