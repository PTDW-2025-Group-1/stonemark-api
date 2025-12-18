package pt.estga.chatbots.core.context;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConversationStateHandlerProvider {

    private final Map<ConversationState, List<ConversationStateHandler>> handlers;

    public ConversationStateHandlerProvider(List<ConversationStateHandler> handlerList) {
        this.handlers = handlerList.stream()
                .collect(Collectors.groupingBy(ConversationStateHandler::canHandle));
    }

    public List<ConversationStateHandler> getHandlers(ConversationState state) {
        return handlers.get(state);
    }
}
