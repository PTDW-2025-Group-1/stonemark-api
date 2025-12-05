package pt.estga.shared.services;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.vonage.client.VonageClient;
import com.vonage.client.sms.MessageStatus;
import com.vonage.client.sms.SmsSubmissionResponse;
import com.vonage.client.sms.messages.TextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsServiceImpl implements SmsService {

    private final VonageClient client;
    private final String fromNumber;
    private final PhoneNumberUtil phoneNumberUtil;

    public SmsServiceImpl(
            @Value("${vonage.api-key}") String apiKey,
            @Value("${vonage.api-secret}") String apiSecret,
            @Value("${vonage.from-number}") String fromNumber
    ) {
        this.client = VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
        this.fromNumber = fromNumber;
        this.phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    public void sendMessage(String phoneNumber, String message) {
        String formattedPhoneNumber;
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "PT"); // Assuming default region as PT (Portugal)
            if (!phoneNumberUtil.isValidNumber(number)) {
                log.error("Invalid phone number: {}", phoneNumber);
                throw new IllegalArgumentException("Invalid phone number format.");
            }
            formattedPhoneNumber = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);
        } catch (NumberParseException e) {
            log.error("Could not parse phone number {}: {}", phoneNumber, e.getMessage());
            throw new IllegalArgumentException("Could not parse phone number.", e);
        }

        TextMessage textMessage = new TextMessage(fromNumber, formattedPhoneNumber, message);

        SmsSubmissionResponse response = client.getSmsClient().submitMessage(textMessage);

        if (response.getMessages().getFirst().getStatus() == MessageStatus.OK) {
            log.info("SMS sent successfully to {}", formattedPhoneNumber);
        } else {
            log.error("Error sending SMS to {}: {}", formattedPhoneNumber,
                    response.getMessages().getFirst().getErrorText());
            throw new RuntimeException("Failed to send SMS");
        }
    }
}
