package pt.estga.bots.telegram.callback;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.bots.telegram.context.ConversationContext;

public interface CallbackQueryHandler {
    BotApiMethod<?> handle(ConversationContext context, String callbackQueryId, String callbackData);
}
