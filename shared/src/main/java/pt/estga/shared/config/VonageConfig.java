package pt.estga.shared.config;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.vonage.client.VonageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VonageConfig {

    @Bean
    public VonageClient vonageClient(
            @Value("${vonage.api-key}") String apiKey,
            @Value("${vonage.api-secret}") String apiSecret
    ) {
        return VonageClient.builder()
                .apiKey(apiKey)
                .apiSecret(apiSecret)
                .build();
    }

    @Bean
    public PhoneNumberUtil phoneNumberUtil() {
        return PhoneNumberUtil.getInstance();
    }
}
