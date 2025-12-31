package pt.estga.chatbot.whatsapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.text.RenderedText;
import pt.estga.chatbot.models.ui.Button;
import pt.estga.chatbot.models.ui.Menu;
import pt.estga.chatbot.models.ui.UIComponent;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WhatsAppApiClient {

    private final WhatsAppTextRenderer textRenderer;

    @Value("${whatsapp.bot.phone-number-id}")
    private String phoneNumberId;

    @Value("${whatsapp.bot.token}")
    private String accessToken;

    public void sendMessage(String phoneNumber, BotResponse response) {
        if (response.getUiComponent() != null) {
            sendUIComponent(phoneNumber, response.getUiComponent(), response);
        } else if (response.getTextNode() != null) {
            RenderedText renderedText = textRenderer.render(response.getTextNode());
            sendTextMessage(phoneNumber, renderedText.text());
        }
    }

    private void sendUIComponent(String phoneNumber, UIComponent uiComponent, BotResponse response) {
        if (uiComponent instanceof Menu menu) {
            sendInteractiveMessage(phoneNumber, menu, response);
        } else {
            // Fallback to text if UI component is not supported yet
            if (response.getTextNode() != null) {
                RenderedText renderedText = textRenderer.render(response.getTextNode());
                sendTextMessage(phoneNumber, renderedText.text());
            }
        }
    }

    private void sendInteractiveMessage(String phoneNumber, Menu menu, BotResponse response) {
        String bodyText = "Please select an option:";
        if (menu.getTitleNode() != null) {
            bodyText = textRenderer.render(menu.getTitleNode()).text();
        } else if (response.getTextNode() != null) {
            bodyText = textRenderer.render(response.getTextNode()).text();
        }

        List<Map<String, Object>> buttons = new ArrayList<>();
        if (menu.getButtons() != null) {
            for (List<Button> row : menu.getButtons()) {
                for (Button button : row) {
                    String buttonText = textRenderer.render(button.getTextNode()).text();
                    // WhatsApp button title limit is 20 chars
                    if (buttonText.length() > 20) {
                        buttonText = buttonText.substring(0, 17) + "...";
                    }

                    buttons.add(Map.of(
                            "type", "reply",
                            "reply", Map.of(
                                    "id", button.getCallbackData() != null ? button.getCallbackData() : "no_id",
                                    "title", buttonText
                            )
                    ));

                    // WhatsApp allows max 3 buttons for quick reply
                    if (buttons.size() >= 3) break;
                }
                if (buttons.size() >= 3) break;
            }
        }

        if (buttons.isEmpty()) {
            sendTextMessage(phoneNumber, bodyText);
            return;
        }

        Map<String, Object> interactive = Map.of(
                "type", "button",
                "body", Map.of("text", bodyText),
                "action", Map.of("buttons", buttons)
        );

        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", phoneNumber,
                "type", "interactive",
                "interactive", interactive
        );

        sendPayload(payload);
    }

    public void sendTextMessage(String userNumber, String messageText) {
        Map<String, Object> payload = Map.of(
                "messaging_product", "whatsapp",
                "to", userNumber,
                "type", "text",
                "text", Map.of("body", messageText)
        );
        sendPayload(payload);
    }

    private void sendPayload(Map<String, Object> payload) {
        WebClient webClient = WebClient.create("https://graph.facebook.com/v24.0/" + phoneNumberId);

        Mono<String> response = webClient.post()
                .uri("/messages")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(payload)
                .retrieve()
                .bodyToMono(String.class);

        response
                .doOnError(e -> log.error("WhatsApp send failed", e))
                .subscribe();
    }
}
