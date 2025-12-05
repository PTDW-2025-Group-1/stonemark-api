package pt.estga.shared.services;

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
    }

    public void sendMessage(String phoneNumber, String message) {
        TextMessage textMessage = new TextMessage(fromNumber, phoneNumber, message);

        SmsSubmissionResponse response = client.getSmsClient().submitMessage(textMessage);

        if (response.getMessages().getFirst().getStatus() == MessageStatus.OK) {
            log.info("SMS sent successfully to {}", phoneNumber);
        } else {
            log.error("Error sending SMS: {}",
                    response.getMessages().getFirst().getErrorText());
            throw new RuntimeException("Failed to send SMS");
        }
    }
}
