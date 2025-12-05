package pt.estga.auth.services.verification.sms;

import pt.estga.auth.entities.token.VerificationToken;

public interface SmsVerificationService {

    void sendVerificationSms(String telephone, VerificationToken token);

}
