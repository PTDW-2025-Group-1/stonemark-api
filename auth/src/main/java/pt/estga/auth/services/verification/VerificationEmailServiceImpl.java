package pt.estga.auth.services.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.services.email.EmailService;
import pt.estga.auth.services.verification.email.EmailContentProvider;
import pt.estga.auth.services.verification.email.EmailContentProviderFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private static final String CONFIRM_PATH = "/confirm?token=";

    private final EmailService emailService;
    private final EmailContentProviderFactory emailContentProviderFactory;

    @Value("${application.frontend-url}")
    private String frontendBaseUrl;

    @Override
    public void sendVerificationEmail(String to, VerificationToken token) {
                EmailContentProvider provider = emailContentProviderFactory.getProvider(token.getPurpose());
        if (provider == null) {
            throw new IllegalArgumentException("No EmailContentProvider found for token purpose: " + token.getPurpose());
        }

        String link = frontendBaseUrl + CONFIRM_PATH + token.getToken();
        long remainingMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();

        Map<String, Object> properties = provider.getProperties(remainingMillis);
        properties.put("link", link);
        properties.put("token", token.getToken());
        properties.put("code", token.getCode());

        Email email = Email.builder()
                .to(to)
                .subject(provider.getSubject())
                .template(provider.getTemplate())
                .properties(properties)
                .build();

        emailService.sendEmail(email);
    }
}
