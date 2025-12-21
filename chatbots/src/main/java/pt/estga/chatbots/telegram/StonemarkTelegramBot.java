package pt.estga.chatbots.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.chatbots.core.shared.services.BotConversationService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;
    private final BotConversationService conversationService;
    private final TelegramAdapter telegramAdapter;
    private final Executor botExecutor;

    public StonemarkTelegramBot(String botUsername, String botToken, String botPath, BotConversationService conversationService, TelegramAdapter telegramAdapter, Executor botExecutor) {
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
            this.execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot commands", e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        CompletableFuture.runAsync(() -> {
            try {
                BotInput botInput = telegramAdapter.toBotInput(update);
                if (botInput != null) {
                    List<BotResponse> botResponses = conversationService.handleInput(botInput);
                    if (botResponses != null) {
                        for (BotResponse botResponse : botResponses) {
                            List<PartialBotApiMethod<?>> methods = telegramAdapter.toBotApiMethod(botInput.getChatId(), botResponse);
                            if (methods != null) {
                                for (PartialBotApiMethod<?> method : methods) {
                                    if (method instanceof BotApiMethod) {
                                        execute((BotApiMethod<?>) method);
                                    } else if (method instanceof SendPhoto) {
                                        execute((SendPhoto) method);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error processing update", e);
            }
        }, botExecutor);
        return null;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
