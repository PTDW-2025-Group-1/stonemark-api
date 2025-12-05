package pt.estga.auth.services.verification.telephone;

import pt.estga.auth.entities.token.VerificationToken;

public interface VerificationTelephoneService {

    void sendVerificationSms(String telephone, VerificationToken token);

}
