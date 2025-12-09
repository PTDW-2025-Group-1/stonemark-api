package pt.estga.auth.services.verification.contact;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.auth.entities.token.VerificationToken;
import pt.estga.shared.services.SmsService;

@Service
@RequiredArgsConstructor
public class SmsVerificationServiceImpl implements SmsVerificationService {

    private final SmsService smsService;

    @Override
    public void sendVerificationSms(String telephone, VerificationToken token) {
        String code = token.getCode();
        String message = "Your verification code is: " + code + ". Valid for 15 minutes.";
        smsService.sendMessage(telephone, message);
    }
}
