package pt.estga.auth.services.verification.sms;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class SmsContentProviderTelephoneVerificationImpl implements SmsContentProvider {

    @Override
    public String getMessage() {
        return "Your telephone verification code is: {CODE}. This code will expire in {EXPIRATION} minutes.";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("EXPIRATION", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
        return properties;
    }

    @Override
    public VerificationPurpose getPurpose() {
        return VerificationPurpose.TELEPHONE_VERIFICATION;
    }
}
