package pt.estga.chatbots.core.context;

import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.BotInput;

public interface ConversationStateHandler {
    BotResponse handle(ConversationContext context, BotInput input);
    ConversationState canHandle();
}
