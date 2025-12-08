package pt.estga.auth.services.tfa;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.auth.entities.TwoFactorCode;
import pt.estga.auth.enums.VerificationTokenPurpose;
import pt.estga.auth.repositories.TwoFactorCodeRepository;
import pt.estga.shared.exceptions.InvalidTokenException;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.services.SmsService;
import pt.estga.user.entities.User;
import pt.estga.user.enums.TfaMethod;
import pt.estga.user.services.UserService;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class ContactBasedTwoFactorAuthenticationServiceImpl implements ContactBasedTwoFactorAuthenticationService {

    private final SmsService smsService;
    private final EmailService emailService;
    private final UserService userService;
    private final TwoFactorCodeRepository twoFactorCodeRepository;

    @Value("${application.security.tfa.code-expiration-minutes:5}")
    private long codeExpirationMinutes;

    private static final int CODE_LENGTH = 6;
    private static final Random random = new Random();

    @Override
    @Transactional
    public void generateAndSendSmsCode(User user) {
        String code = generateRandomCode();
        saveTwoFactorCode(user, code, VerificationTokenPurpose.SMS_2FA);

        userService.getPrimaryTelephone(user)
                .ifPresentOrElse(
                        telephone -> smsService.sendMessage(telephone, "Your 2FA code is: " + code),
                        () -> { throw new IllegalStateException("User has no primary telephone for SMS 2FA."); }
                );
    }

    @Override
    @Transactional
    public void generateAndSendEmailCode(User user) {
        String code = generateRandomCode();
        saveTwoFactorCode(user, code, VerificationTokenPurpose.EMAIL_2FA);

        userService.getPrimaryEmail(user)
                .ifPresentOrElse(
                        email -> {
                            Map<String, Object> properties = new HashMap<>();
                            properties.put("code", code);
                            emailService.sendEmail(Email.builder()
                                    .to(email)
                                    .subject("Two-Factor Authentication Code")
                                    .template("email/tfa-code.html")
                                    .properties(properties)
                                    .build());
                        },
                        () -> { throw new IllegalStateException("User has no primary email for Email 2FA."); }
                );
    }

    @Override
    @Transactional
    public boolean verifyCode(User user, String code, VerificationTokenPurpose purpose) {
        TwoFactorCode twoFactorCode = twoFactorCodeRepository.findByUserAndPurpose(user, purpose)
                .orElseThrow(() -> new InvalidTokenException("2FA code not found or expired."));

        if (twoFactorCode.getExpiryDate().isBefore(Instant.now())) {
            twoFactorCodeRepository.delete(twoFactorCode);
            throw new InvalidTokenException("2FA code expired.");
        }

        if (twoFactorCode.getCode().equals(code)) {
            twoFactorCodeRepository.delete(twoFactorCode); // Code used, delete it
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
        if (user.getTfaMethod() == TfaMethod.SMS) {
            return verifyCode(user, code, VerificationTokenPurpose.SMS_2FA);
        } else if (user.getTfaMethod() == TfaMethod.EMAIL) {
            return verifyCode(user, code, VerificationTokenPurpose.EMAIL_2FA);
        }
        return false; // Or throw an exception if contact-based 2FA is not enabled
    }

    private String generateRandomCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            code.append(random.nextInt(10)); // 0-9
        }
        return code.toString();
    }

    private void saveTwoFactorCode(User user, String code, VerificationTokenPurpose purpose) {
        // Delete any existing code for this purpose
        twoFactorCodeRepository.deleteByUserAndPurpose(user, purpose);

        TwoFactorCode twoFactorCode = TwoFactorCode.builder()
                .user(user)
                .code(code)
                .purpose(purpose)
                .expiryDate(Instant.now().plusSeconds(codeExpirationMinutes * 60))
                .build();
        twoFactorCodeRepository.save(twoFactorCode);
    }
}
