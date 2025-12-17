package pt.estga.chatbots.telegram.handlers;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface UpdateHandler {
    BotApiMethod<?> handle(Update update);
}
