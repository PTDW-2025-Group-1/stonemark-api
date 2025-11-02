package pt.estga.stonemark.services.security.verification;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.stonemark.entities.token.VerificationToken;
import pt.estga.stonemark.models.Email;
import pt.estga.stonemark.services.email.EmailService;
import pt.estga.stonemark.services.security.verification.email.EmailContentProvider;
import pt.estga.stonemark.services.security.verification.email.EmailContentProviderFactory;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class VerificationEmailServiceImpl implements VerificationEmailService {

    private static final String CONFIRM_PATH = "/api/v1/auth/confirm?token=";

    private final EmailService emailService;
    private final EmailContentProviderFactory emailContentProviderFactory;

    @Value("${application.base-url}")
    private String backendBaseUrl;

    @Override
    public void sendVerificationEmail(String to, VerificationToken token) {
                EmailContentProvider provider = emailContentProviderFactory.getProvider(token.getPurpose());
        if (provider == null) {
            throw new IllegalArgumentException("No EmailContentProvider found for token purpose: " + token.getPurpose());
        }

        String link = backendBaseUrl + CONFIRM_PATH + token.getToken();
        long remainingMillis = token.getExpiresAt().toEpochMilli() - System.currentTimeMillis();

        Map<String, Object> properties = provider.getProperties(remainingMillis);
        properties.put("link", link);
        properties.put("token", token.getToken());

        Email email = Email.builder()
                .to(to)
                .subject(provider.getSubject())
                .template(provider.getTemplate())
                .properties(properties)
                .build();

        emailService.sendEmail(email);
    }
}
