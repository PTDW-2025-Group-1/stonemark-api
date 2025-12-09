package pt.estga.auth.services.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class EmailContentProviderEmailVerificationImpl implements EmailContentProvider {

    @Override
    public String getSubject() {
        return "Please verify your email";
    }

    @Override
    public String getTemplate() {
        return "email/email-verification.html";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("expiration", TimeUnit.MILLISECONDS.toHours(remainingMillis));
        return properties;
    }

    @Override
    public VerificationPurpose getPurpose() {
        return VerificationPurpose.EMAIL_VERIFICATION;
    }
}
