package pt.estga.chatbots.core.shared.context;

import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.BotInput;

import java.util.List;

public interface ConversationStateHandler {
    List<BotResponse> handle(ConversationContext context, BotInput input);
    ConversationState canHandle();
}
