package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.auth.services.verification.contact.EmailVerificationService;
import pt.estga.auth.services.verification.contact.SmsVerificationService;
import pt.estga.user.entities.UserContact;
import pt.estga.user.enums.ContactType;

@Service
@RequiredArgsConstructor
public class VerificationDispatchServiceImpl implements VerificationDispatchService {

    private final EmailVerificationService emailVerificationService;
    private final SmsVerificationService smsVerificationService;

    @Override
    public void sendVerification(UserContact userContact, VerificationToken token) {
        if (userContact.getType() == ContactType.EMAIL) {
            emailVerificationService.sendVerificationEmail(userContact.getValue(), token);
        } else if (userContact.getType() == ContactType.TELEPHONE) {
            smsVerificationService.sendVerificationSms(userContact.getValue(), token);
        } else {
            throw new IllegalArgumentException("Unsupported contact type for verification: " + userContact.getType());
        }
    }
}
