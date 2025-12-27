package pt.estga.chatbot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.HandlerOutcome;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationFlowManager {

    private final List<FlowStrategy> flowStrategies;

    public ConversationState getNextState(ChatbotContext context, ConversationState currentState, HandlerOutcome outcome) {
        for (FlowStrategy strategy : flowStrategies) {
            if (strategy.supports(currentState)) {
                return strategy.getNextState(context, currentState, outcome);
            }
        }
        // Fallback or error state if no strategy supports the current state
        return currentState;
    }
}
