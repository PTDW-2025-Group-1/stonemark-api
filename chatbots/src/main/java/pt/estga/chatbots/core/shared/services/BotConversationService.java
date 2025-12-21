package pt.estga.chatbots.core.shared.services;

import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

public interface BotConversationService {
    List<BotResponse> handleInput(BotInput input);
}
