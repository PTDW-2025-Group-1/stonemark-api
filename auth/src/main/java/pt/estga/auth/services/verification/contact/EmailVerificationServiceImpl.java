package pt.estga.auth.services.verification.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.models.Email;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final EmailService emailService;

    @Override
    public void sendVerificationEmail(String to, VerificationToken token) {
        Email email = Email.builder()
                .to(to)
                .subject("Your Verification Code")
                .template("email/tfa-code")
                .properties(Map.of("code", token.getCode()))
                .build();

        emailService.sendEmail(email);
    }
}
