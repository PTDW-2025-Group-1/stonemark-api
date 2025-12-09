package pt.estga.auth.services.verification.sms;

import pt.estga.auth.enums.VerificationPurpose;

import java.util.Map;

public interface SmsContentProvider {

    String getMessage();

    Map<String, Object> getProperties(long remainingMillis);

    VerificationPurpose getPurpose();

}
