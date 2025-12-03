package pt.estga.user.service;

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

    public void sendVerificationCode(String phoneNumber, String code) {
        TextMessage message = new TextMessage(
                fromNumber,
                phoneNumber,
                "Your Stonemark verification code is: " + code + ". Valid for 15 minutes."
        );

        SmsSubmissionResponse response = client.getSmsClient().submitMessage(message);

        if (response.getMessages().get(0).getStatus() == MessageStatus.OK) {
            log.info("SMS enviado com sucesso para {}", phoneNumber);
        } else {
            log.error("Erro ao enviar SMS: {}",
                    response.getMessages().get(0).getErrorText());
            throw new RuntimeException("Failed to send SMS");
        }
    }
}
