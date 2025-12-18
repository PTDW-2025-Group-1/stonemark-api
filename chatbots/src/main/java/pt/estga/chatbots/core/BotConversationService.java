package pt.estga.chatbots.core;

import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;

public interface BotConversationService {
    BotResponse handleInput(BotInput input);
}
