package pt.estga.auth.services.tfa;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;
import pt.estga.verification.repositories.ActionCodeRepository;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.services.SmsService;
import pt.estga.user.entities.User;
import pt.estga.user.enums.ContactType;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserContactService;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TwoFactorAuthenticationServiceImpl implements TwoFactorAuthenticationService {

    private final SmsService smsService;
    private final EmailService emailService;
    private final UserContactService userContactService;
    private final ActionCodeRepository actionCodeRepository;
    private final UserService userService;
    private final TotpService totpService;

    @Value("${application.security.tfa.code-expiration-minutes:5}")
    private long codeExpirationMinutes;

    @Override
    @Transactional
    public void generateAndSendSmsCode(User user) {
        String code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        saveActionCode(user, code);

        userContactService.findPrimary(user, ContactType.TELEPHONE)
                .ifPresentOrElse(
                        telephone -> smsService.sendMessage(telephone.getValue(), "Your 2FA code is: " + code),
                        () -> { throw new IllegalStateException("User has no primary telephone for SMS 2FA."); }
                );
    }

    @Override
    @Transactional
    public void generateAndSendEmailCode(User user) {
        String code = RandomStringUtils.randomAlphanumeric(6).toUpperCase();
        saveActionCode(user, code);

        userContactService.findPrimary(user, ContactType.EMAIL)
                .ifPresentOrElse(
                        email -> {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put("code", code);
                            emailService.sendEmail(Email.builder()
                                    .to(email.getValue())
                                    .subject("Two-Factor Authentication Code")
                                    .template("username/tfa-code.html")
                                    .properties(properties)
                                    .build());
                        },
                        () -> { throw new IllegalStateException("User has no primary username for Email 2FA."); }
                );
    }

    @Override
    @Transactional
    public boolean verifyCode(User user, String code, ActionCodeType type) {
        ActionCode actionCode = actionCodeRepository.findByUserAndType(user, type)
                .orElseThrow(() -> new InvalidTokenException("2FA code not found or expired."));

        if (actionCode.getExpiresAt().isBefore(Instant.now())) {
            actionCodeRepository.delete(actionCode);
            throw new InvalidTokenException("2FA code expired.");
        }

        if (actionCode.getCode().equals(code)) {
            actionCodeRepository.delete(actionCode); // Code used, delete it
            return true;
        }
        return false;
    }

    @Override
    @Transactional
    public void requestTfaContactCode(User user) {
        if (user.getTfaMethod() == TfaMethod.SMS) {
            generateAndSendSmsCode(user);
        } else if (user.getTfaMethod() == TfaMethod.EMAIL) {
            generateAndSendEmailCode(user);
        } else {
            throw new IllegalStateException("Contact-based 2FA is not enabled for this user.");
        }
    }

    @Override
    @Transactional
    public boolean verifyTfaContactCode(User user, String code) {
        if (user.getTfaMethod() == TfaMethod.SMS || user.getTfaMethod() == TfaMethod.EMAIL) {
            return verifyCode(user, code, ActionCodeType.TWO_FACTOR);
        }
        return false;
    }

    @Override
    @Transactional
    public void setTfaMethod(User user, TfaMethod method) {
        if (user.getTfaMethod() == TfaMethod.TOTP && method != TfaMethod.TOTP) {
            totpService.disableTfa(user);
        }

        user.setTfaMethod(method);
        if (method == TfaMethod.TOTP) {
            totpService.enableTfa(user, method);
        } else if (method == TfaMethod.NONE) {
            totpService.disableTfa(user);
        }
        userService.update(user);
    }

    private void saveActionCode(User user, String code) {
        // Delete any existing code for this purpose
        actionCodeRepository.deleteByUserAndType(user, ActionCodeType.TWO_FACTOR);

        ActionCode actionCode = ActionCode.builder()
                .user(user)
                .code(code)
                .type(ActionCodeType.TWO_FACTOR)
                .expiresAt(Instant.now().plusSeconds(codeExpirationMinutes * 60))
                .build();
        actionCodeRepository.save(actionCode);
    }
}
