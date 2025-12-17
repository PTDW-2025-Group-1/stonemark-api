package pt.estga.chatbots.telegram.handlers;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.chatbots.telegram.services.TelegramBotCommandService;

@RequiredArgsConstructor
public class CallbackQueryHandler implements UpdateHandler {

    private final TelegramBotCommandService commandService;

    @Override
    public BotApiMethod<?> handle(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        return commandService.handleCallbackQuery(
                callbackQuery.getMessage().getChatId(),
                callbackQuery.getId(),
                callbackQuery.getData()
        );
    }
}
