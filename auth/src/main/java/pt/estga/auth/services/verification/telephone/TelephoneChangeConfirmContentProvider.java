package pt.estga.auth.services.verification.telephone;

import org.springframework.stereotype.Component;
import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class TelephoneChangeConfirmContentProvider implements TelephoneContentProvider {

    @Override
    public String getMessage() {
        return "Your telephone change confirmation code is: {CODE}. This code will expire in {EXPIRATION} minutes.";
    }

    @Override
    public Map<String, Object> getProperties(long remainingMillis) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("EXPIRATION", TimeUnit.MILLISECONDS.toMinutes(remainingMillis));
        return properties;
    }

    @Override
    public VerificationTokenPurpose getPurpose() {
        return VerificationTokenPurpose.TELEPHONE_CHANGE_CONFIRM;
    }
}
