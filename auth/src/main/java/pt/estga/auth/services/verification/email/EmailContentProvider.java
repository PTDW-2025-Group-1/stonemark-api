package pt.estga.auth.services.verification.email;

import pt.estga.stonemark.enums.VerificationTokenPurpose;

import java.util.Map;

public interface EmailContentProvider {

    String getSubject();

    String getTemplate();

    Map<String, Object> getProperties(long remainingMillis);

    VerificationTokenPurpose getPurpose();
}
