package pt.estga.chatbot.services;

import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.HandlerOutcome;

public interface FlowStrategy {
    boolean supports(ConversationState state);
    ConversationState getNextState(ChatbotContext context, ConversationState currentState, HandlerOutcome outcome);
}
