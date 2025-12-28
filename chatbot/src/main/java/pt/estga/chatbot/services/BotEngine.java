package pt.estga.chatbot.services;

import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;

import java.util.List;

public interface BotEngine {
    List<BotResponse> handleInput(BotInput input);
}
