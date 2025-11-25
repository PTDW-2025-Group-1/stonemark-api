package pt.estga.auth.services.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TwoFactorAuthContentProvider implements EmailContentProvider {

    @Override
    public String getSubject() {
        return "Two-Factor Authentication";
    }

    @Override
    public String getTemplate() {
        return "email/two-factor-authentication.html";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
        return properties;
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TWO_FACTOR_AUTHENTICATION;
    }
}
