package pt.estga.chatbots.telegram.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import pt.estga.chatbots.telegram.services.TelegramBotCommandService;

import java.util.Map;
import java.util.function.Function;

public class CommandHandler {

    private final Map<String, Function<Long, BotApiMethod<?>>> commandRegistry;

    public CommandHandler(TelegramBotCommandService commandService) {
        this.commandRegistry = Map.of(
                "/start", commandService::handleStartCommand,
                "/submit", commandService::handleSubmitCommand,
                "/cancel", commandService::handleCancelCommand,
                "/help", commandService::handleHelpCommand,
                "/skip", commandService::handleSkipCommand
        );
    }

    public BotApiMethod<?> handle(Message message) {
        long chatId = message.getChatId();
        String text = message.getText();

        if (text != null && text.startsWith("/")) {
            String command = text.split(" ")[0];
            Function<Long, BotApiMethod<?>> action = commandRegistry.get(command);
            if (action != null) {
                return action.apply(chatId);
            }
        }
        return null;
    }
}
