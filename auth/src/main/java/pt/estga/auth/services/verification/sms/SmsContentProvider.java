package pt.estga.auth.services.verification.sms;

import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.Map;

public interface SmsContentProvider {

    String getMessage();

    Map<String, Object> getProperties(long remainingMillis);

    VerificationTokenPurpose getPurpose();

}
