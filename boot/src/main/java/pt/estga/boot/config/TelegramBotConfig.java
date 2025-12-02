package pt.estga.boot.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.estga.bots.telegram.StonemarkTelegramBot;
import pt.estga.bots.telegram.TelegramBotCommandService;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String webhookPath;

    @Bean
    public StonemarkTelegramBot stonemarkTelegramBot(TelegramBotCommandService commandService) {
        return new StonemarkTelegramBot(botUsername, botToken, webhookPath, commandService);
    }
}
