package pt.estga.chatbots.telegram.handlers;

import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.chatbots.telegram.StonemarkTelegramBot;
import pt.estga.chatbots.telegram.services.TelegramBotCommandService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpdateHandlerFactory {

    private final Map<UpdateType, UpdateHandler> handlers = new HashMap<>();

    public UpdateHandlerFactory(StonemarkTelegramBot bot, TelegramBotCommandService commandService) {
        handlers.put(UpdateType.MESSAGE, new MessageHandler(bot, commandService));
        handlers.put(UpdateType.CALLBACK_QUERY, new CallbackQueryHandler(commandService));
    }

    public Optional<UpdateHandler> getHandler(Update update) {
        UpdateType type = UpdateType.from(update);
        return Optional.ofNullable(handlers.get(type));
    }

    private enum UpdateType {
        MESSAGE,
        CALLBACK_QUERY,
        UNKNOWN;

        public static UpdateType from(Update update) {
            if (update.hasMessage()) {
                return MESSAGE;
            } else if (update.hasCallbackQuery()) {
                return CALLBACK_QUERY;
            }
            return UNKNOWN;
        }
    }
}
