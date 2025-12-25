package pt.estga.chatbot.telegram;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {
    private Auth auth = new Auth();

    @Data
    public static class Auth {
        private boolean enabled;
    }
}
