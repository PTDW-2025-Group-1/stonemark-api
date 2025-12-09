package pt.estga.auth.services.verification.email;

import pt.estga.auth.enums.VerificationPurpose;

import java.util.Map;

public interface EmailContentProvider {

    String getSubject();

    String getTemplate();

    Map<String, Object> getProperties(long remainingMillis);

    VerificationPurpose getPurpose();
}
