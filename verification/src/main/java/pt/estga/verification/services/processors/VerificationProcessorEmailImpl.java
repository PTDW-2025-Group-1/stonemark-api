package pt.estga.verification.services.processors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.user.entities.UserContact;
import pt.estga.verification.entities.ActionCode;
import pt.estga.verification.enums.ActionCodeType;

import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class VerificationProcessorEmailImpl implements VerificationProcessor {

    private final EmailService emailService;

    @Override
    public Optional<String> process(UserContact userContact, ActionCode code) {
        if (userContact == null) {
            throw new IllegalArgumentException("UserContact cannot be null for email verification.");
        }
        Email email = Email.builder()
                .to(userContact.getValue())
                .subject("Verify your email")
                .template("email/email-verification")
                .properties(Map.of("code", code.getCode()))
                .build();
        emailService.sendEmail(email);
        return Optional.empty();
    }

    @Override
    public ActionCodeType getType() {
        return ActionCodeType.EMAIL_VERIFICATION;
    }
}
