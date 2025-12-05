package pt.estga.auth.services.verification.telephone;

import pt.estga.auth.enums.VerificationTokenPurpose;

import java.util.Map;

public interface TelephoneContentProvider {

    String getMessage();

    Map<String, Object> getProperties(long remainingMillis);

    VerificationTokenPurpose getPurpose();

}
