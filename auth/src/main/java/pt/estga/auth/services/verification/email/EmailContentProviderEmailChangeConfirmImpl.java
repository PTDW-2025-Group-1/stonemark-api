package pt.estga.auth.services.verification.email;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class EmailContentProviderEmailChangeConfirmImpl implements EmailContentProvider {

    @Override
    public String getSubject() {
        return "Confirm Your New Email Address";
    }

    @Override
    public String getTemplate() {
        return "email/email-change-confirm.html";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("expiration", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
        return properties;
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.EMAIL_CHANGE_CONFIRM;
    }
}
