package pt.estga.verification.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.shared.models.Email;
import pt.estga.shared.services.EmailService;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;
import pt.estga.user.repositories.UserContactRepository;
import pt.estga.verification.entities.ActionCode;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserContactActivationService {

    private final ActionCodeService actionCodeService;
    private final UserContactRepository userContactRepository;
    private final EmailService emailService;

    @Transactional
    public Optional<String> activateUserContact(ActionCode actionCode) {
        log.info("Activating user contact for action code: {}", actionCode.getCode());

        UserContact contactToVerify = actionCode.getUserContact();

        if (contactToVerify == null) {
            throw new IllegalStateException("ActionCode has no associated UserContact.");
        }

        log.info(
                "Verifying contact id={} value={}",
                contactToVerify.getId(),
                contactToVerify.getValue()
        );

        if (contactToVerify.isVerified()) {
            log.warn("Contact {} is already verified.", contactToVerify.getValue());
            actionCodeService.consumeCode(actionCode);
            return Optional.empty();
        }

        contactToVerify.setVerified(true);
        contactToVerify.setVerifiedAt(Instant.now());
        userContactRepository.save(contactToVerify);

        actionCodeService.consumeCode(actionCode);

        sendConfirmationEmail(contactToVerify);

        return Optional.empty();
    }

    private UserContact findContactForActionCode(ActionCode actionCode) {
        ContactType expectedContactType = getContactTypeForAction(actionCode.getType());
        return actionCode.getUser().getContacts().stream()
                .filter(contact -> contact.getType() == expectedContactType) // Removed !contact.isVerified()
                .findFirst()
                .orElseThrow(() -> {
                    log.error("No contact of type {} found for user {}", expectedContactType, actionCode.getUser().getId());
                    return new IllegalStateException("No matching contact found for the action code."); // Adjusted message
                });
    }

    private void sendConfirmationEmail(UserContact userContact) {
        if (userContact.getType() == ContactType.EMAIL) {
            String to = userContact.getValue();
            String subject = "Email Address Verified";
            Email email = Email.builder()
                    .to(to)
                    .subject(subject)
                    .template("email/email-verification-confirmation")
                    .properties(Map.of("name", userContact.getUser().getFirstName()))
                    .build();
            emailService.sendEmail(email);
            log.info("Sent verification confirmation email to {}", to);
        }
    }

    private ContactType getContactTypeForAction(pt.estga.verification.enums.ActionCodeType actionCodeType) {
        return switch (actionCodeType) {
            case EMAIL_VERIFICATION -> ContactType.EMAIL;
            case PHONE_VERIFICATION -> ContactType.TELEPHONE;
            default -> throw new IllegalArgumentException("Unsupported action code type for contact activation: " + actionCodeType);
        };
    }
}
