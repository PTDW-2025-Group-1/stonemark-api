package pt.estga.stonemark.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import pt.estga.stonemark.bots.telegram.StonemarkTelegramBot;

@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String webhookPath;

    @Bean
    public StonemarkTelegramBot stonemarkTelegramBot() {
        return new StonemarkTelegramBot(botUsername, botToken, webhookPath);
    }
}
