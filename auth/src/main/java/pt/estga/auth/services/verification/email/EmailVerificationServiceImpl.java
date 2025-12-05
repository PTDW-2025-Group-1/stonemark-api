package pt.estga.auth.services.verification.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.shared.services.EmailService;
import pt.estga.shared.models.Email;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    @Value("${application.verification.confirm-path:/confirm?token=}")
    private String confirmPath;

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

        String link = frontendBaseUrl + confirmPath + token.getToken();
        long remainingMillis = token.getRemainingValidityMillis();

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
