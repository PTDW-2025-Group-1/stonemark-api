package pt.estga.chatbots.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.chatbots.telegram.handlers.UpdateHandlerFactory;
import pt.estga.chatbots.telegram.services.TelegramBotCommandService;

import java.util.List;

@Slf4j
public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;
    private final UpdateHandlerFactory updateHandlerFactory;

    public StonemarkTelegramBot(String botUsername, String botToken, String botPath, TelegramBotCommandService commandService) {
        super(botToken);
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.updateHandlerFactory = new UpdateHandlerFactory(this, commandService);
        setBotCommands();
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "Start a new conversation")
        );

        try {
            this.execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot commands", e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        return updateHandlerFactory.getHandler(update)
                .map(handler -> handler.handle(update))
                .orElseGet(() -> {
                    log.warn("Received an unhandled update type: {}", update);
                    return null;
                });
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
