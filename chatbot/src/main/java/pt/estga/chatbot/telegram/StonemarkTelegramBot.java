package pt.estga.chatbot.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.chatbot.services.BotConversationService;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;
    private final BotConversationService conversationService;
    private final TelegramAdapter telegramAdapter;
    private final Executor botExecutor;

    public StonemarkTelegramBot(String botUsername,
                                String botToken,
                                String botPath,
                                BotConversationService conversationService,
                                TelegramAdapter telegramAdapter,
                                Executor botExecutor) {
        super(botToken);
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.conversationService = conversationService;
        this.telegramAdapter = telegramAdapter;
        this.botExecutor = botExecutor;
        setBotCommands();
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "Start a new conversation"),
                new BotCommand("options", "Show main options")
        );
        try {
            this.execute(new org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot commands", e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        // Run asynchronously with executor (SecurityContext is already propagated if using DelegatingSecurityContextExecutor)
        CompletableFuture.runAsync(() -> {
            try {
                BotInput botInput = telegramAdapter.toBotInput(update);
                if (botInput != null) {
                    List<BotResponse> botResponses = conversationService.handleInput(botInput);
                    if (botResponses != null) {
                        for (BotResponse response : botResponses) {
                            List<PartialBotApiMethod<?>> methods = telegramAdapter.toBotApiMethod(botInput.getChatId(), response);
                            if (methods != null) {
                                for (PartialBotApiMethod<?> method : methods) {
                                    if (method instanceof BotApiMethod) execute((BotApiMethod<?>) method);
                                    else if (method instanceof SendPhoto) execute((SendPhoto) method);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing Telegram update", e);
            }
        }, botExecutor);

        // Telegram webhook can return null since responses are sent asynchronously
        return null;
    }

    @Override
    public String getBotUsername() { return botUsername; }

    @Override
    public String getBotPath() { return botPath; }
}
