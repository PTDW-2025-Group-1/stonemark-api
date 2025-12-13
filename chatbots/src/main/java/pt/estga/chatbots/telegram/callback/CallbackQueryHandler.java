package pt.estga.chatbots.telegram.callback;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.chatbots.telegram.context.ConversationContext;

public interface CallbackQueryHandler {
    BotApiMethod<?> handle(ConversationContext context, String callbackQueryId, String callbackData);
}
