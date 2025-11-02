package pt.estga.stonemark.services.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pt.estga.stonemark.entities.User;
import pt.estga.stonemark.entities.request.EmailChangeRequest;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.enums.VerificationTokenPurpose;
import pt.estga.stonemark.exceptions.EmailAlreadyTakenException;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.repositories.EmailChangeRequestRepository;
import pt.estga.stonemark.services.UserService;
import pt.estga.stonemark.services.email.EmailService;
import pt.estga.stonemark.services.token.VerificationTokenService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VerificationInitiationServiceImpl implements VerificationInitiationService {

    private static final String CONFIRM_PATH = "/api/v1/auth/confirm?token=";

    private final VerificationTokenService verificationTokenService;
    private final EmailService emailService;
    private final EmailChangeRequestRepository emailChangeRequestRepository;
    private final UserService userService;

    @Value("${application.base-url}")
    private String backendBaseUrl;

    @Transactional
    @Override
    public void createAndSendToken(User user, VerificationTokenPurpose purpose) {
        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, purpose);
        sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Override
    public void requestEmailChange(User user, String newEmail) {
        if (userService.existsByEmail(newEmail)) {
            throw new EmailAlreadyTakenException("Email is already taken.");
        }

        VerificationToken verificationToken = verificationTokenService.createAndSaveToken(user, VerificationTokenPurpose.EMAIL_CHANGE_REQUEST);

        EmailChangeRequest emailChangeRequest = EmailChangeRequest.builder()
                .user(user)
                .newEmail(newEmail)
                .verificationToken(verificationToken)
                .build();

        emailChangeRequestRepository.save(emailChangeRequest);

        sendVerificationEmail(user.getEmail(), verificationToken);
    }

    @Override
    public void sendEmailChangeConfirmation(String newEmail, VerificationToken confirmationToken) {
        sendVerificationEmail(newEmail, confirmationToken);
    }

    private void sendVerificationEmail(String to, VerificationToken token) {
        String link = backendBaseUrl + CONFIRM_PATH + token.getToken();
        long remainingMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();

        Email.EmailBuilder emailBuilder = Email.builder().to(to);
        Map<String, Object> properties = new HashMap<>();
        properties.put("link", link);
        properties.put("token", token.getToken());

        switch (token.getPurpose()) {
            case EMAIL_VERIFICATION -> {
                emailBuilder.subject("Please verify your email");
                emailBuilder.template("email/email-verification.html");
                properties.put("expiration", TimeUnit.MILLISECONDS.toHours(remainingMillis));
            }
            case PASSWORD_RESET -> {
                emailBuilder.subject("Password Reset Request");
                emailBuilder.template("email/password-reset.html");
                properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            }
            case TWO_FACTOR_AUTHENTICATION -> {
                emailBuilder.subject("Two-Factor Authentication");
                emailBuilder.template("email/two-factor-authentication.html");
                properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            }
            case EMAIL_CHANGE_REQUEST -> {
                emailBuilder.subject("Confirm Your Email Change Request");
                emailBuilder.template("email/email-change-request.html");
                properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            }
            case EMAIL_CHANGE_CONFIRM -> {
                emailBuilder.subject("Confirm Your New Email Address");
                emailBuilder.template("email/email-change-confirm.html");
                properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
            }
        }

        emailBuilder.properties(properties);
        emailService.sendEmail(emailBuilder.build());
    }
}
