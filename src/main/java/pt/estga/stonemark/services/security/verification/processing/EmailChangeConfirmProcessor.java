package pt.estga.stonemark.services.security.verification.processing;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.InvalidTokenException;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.email.EmailService;

@Component
@RequiredArgsConstructor
public class EmailChangeConfirmProcessor implements VerificationProcessor {

    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserService userService;
    private final EmailService emailService;

    @Override
    public void process(VerificationToken token) {
        EmailChangeRequest emailChangeRequest = emailChangeRequestRepository.findByVerificationToken(token)
                .orElseThrow(() -> new InvalidTokenException("Email change request not found."));

        User user = emailChangeRequest.getUser();
        String newEmail = emailChangeRequest.getNewEmail();

        emailService.sendEmail(Email.builder()
                .to(user.getEmail())
                .subject("Email Address Changed")
                .template("email/email-changed-notification.html")
                .build());

        user.setEmail(newEmail);
        user.setGoogleId(null);
        userService.update(user);

        emailChangeRequestRepository.delete(emailChangeRequest);
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM;
    }
}
