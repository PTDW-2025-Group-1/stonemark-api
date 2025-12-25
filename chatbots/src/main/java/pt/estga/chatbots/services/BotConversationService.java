package pt.estga.chatbots.services;

import pt.estga.chatbots.models.BotInput;
import pt.estga.chatbots.models.BotResponse;

import java.util.List;

public interface BotConversationService {
    List<BotResponse> handleInput(BotInput input);
}
