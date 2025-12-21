package pt.estga.chatbots.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import pt.estga.chatbots.core.shared.services.BotConversationService;
import pt.estga.chatbots.telegram.StonemarkTelegramBot;
import pt.estga.chatbots.telegram.TelegramAdapter;

import java.util.concurrent.Executor;

/**
 * Configuration class for the Telegram Bot integration.
 * Defines the bot bean and its specific execution context to ensure
 * asynchronous processing does not impact the main application threads.
 */
@Configuration
public class TelegramBotConfig {

    @Value("${telegram.bot.username}")
    private String botUsername;

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.webhook-path}")
    private String webhookPath;

    /**
     * Creates a dedicated Executor for Bot tasks to ensure isolation and scalability.
     */
    @Bean(name = "botTaskExecutor")
    public Executor botTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("BotAsync-");
        executor.initialize();
        return executor;
    }

    /**
     * Configures the main Telegram Bot bean.
     *
     * @param conversationService Service handling business logic and conversation flow.
     * @param telegramAdapter     Adapter to convert Telegram objects to domain objects.
     * @param botTaskExecutor     Dedicated executor for asynchronous update processing.
     */
    @Bean
    public StonemarkTelegramBot stonemarkTelegramBot(
            BotConversationService conversationService,
            TelegramAdapter telegramAdapter,
            @Qualifier("botTaskExecutor") Executor botTaskExecutor) {
        return new StonemarkTelegramBot(botUsername, botToken, webhookPath,
                conversationService, telegramAdapter, botTaskExecutor);
    }
}
