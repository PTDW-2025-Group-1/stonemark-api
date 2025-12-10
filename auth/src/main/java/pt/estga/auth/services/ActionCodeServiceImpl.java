package pt.estga.auth.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.ActionCode;
import pt.estga.auth.enums.ActionCodeType;
import pt.estga.auth.repositories.ActionCodeRepository;
import pt.estga.user.entities.User;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActionCodeServiceImpl implements ActionCodeService {

    private final ActionCodeRepository actionCodeRepository;

    @Value("${application.security.action-code.email-verification.expiration}")
    private long emailVerificationExpiration;

    @Value("${application.security.action-code.password-reset.expiration}")
    private long passwordResetExpiration;

    @Value("${application.security.action-code.two-factor.expiration}")
    private long twoFactorExpiration;

    @Value("${application.security.action-code.telephone-verification.expiration}")
    private long telephoneVerificationExpiration;

    @Value("${application.security.action-code.device-verification.expiration}")
    private long deviceVerificationExpiration;

    @Override
    public ActionCode createAndSave(User user, ActionCodeType type) {
        long expirationMillis = getExpirationMillisFor(type);

        String code;
        do {
            code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        } while (actionCodeRepository.findByUserAndType(user, type).isPresent());

        ActionCode actionCode = ActionCode.builder()
                .code(code)
                .user(user)
                .type(type)
                .expiresAt(Instant.now().plusMillis(expirationMillis))
                .consumed(false)
                .build();

        return actionCodeRepository.save(actionCode);
    }

    @Override
    public Optional<ActionCode> findByCode(String code) {
        return actionCodeRepository.findAll().stream()
                .filter(ac -> ac.getCode().equals(code) && !ac.isConsumed())
                .findFirst();
    }

    @Override
    public boolean isCodeValid(String code) {
        return findByCode(code)
                .map(ac -> !ac.isConsumed() && ac.getExpiresAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public void consumeCode(ActionCode code) {
        code.setConsumed(true);
        actionCodeRepository.save(code);
    }

    private long getExpirationMillisFor(ActionCodeType type) {
        return switch (type) {
            case EMAIL_VERIFICATION -> emailVerificationExpiration;
            case PHONE_VERIFICATION -> telephoneVerificationExpiration;
            case RESET_PASSWORD -> passwordResetExpiration;
            case TWO_FACTOR -> twoFactorExpiration;
            case DEVICE_VERIFICATION -> deviceVerificationExpiration;
        };
    }
}
