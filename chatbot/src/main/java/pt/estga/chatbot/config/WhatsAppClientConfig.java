package pt.estga.chatbot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WhatsAppClientConfig {

    @Bean
    public WebClient whatsappWebClient(
            @Value("${application.base-url}") String baseUrl,
            @Value("${whatsapp.bot.webhook-path}") String webhookUrl,
            @Value("${whatsapp.bot.token}") String token
    ) {
        return WebClient.builder()
                .baseUrl(baseUrl + webhookUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }
}
