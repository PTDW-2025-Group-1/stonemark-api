package pt.estga.auth.services.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class EmailContentProviderPasswordResetImpl implements EmailContentProvider {

    @Override
    public String getSubject() {
        return "Password Reset Request";
    }

    @Override
    public String getTemplate() {
        return "email/password-reset.html";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
        return properties;
    }

    @Override
    public VerificationPurpose getPurpose() {
        return VerificationPurpose.PASSWORD_RESET;
    }
}
