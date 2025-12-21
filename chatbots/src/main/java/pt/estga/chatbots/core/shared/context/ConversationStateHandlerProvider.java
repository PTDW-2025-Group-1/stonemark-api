package pt.estga.chatbots.core.shared.context;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ConversationStateHandlerProvider {

    private final Map<ConversationState, List<ConversationStateHandler>> handlers;
    private final List<ConversationStateHandler> globalHandlers;

    public ConversationStateHandlerProvider(List<ConversationStateHandler> handlerList) {
        this.handlers = handlerList.stream()
                .filter(h -> h.canHandle() != null)
                .collect(Collectors.groupingBy(ConversationStateHandler::canHandle));
        this.globalHandlers = handlerList.stream()
                .filter(h -> h.canHandle() == null)
                .collect(Collectors.toList());
    }

    public List<ConversationStateHandler> getHandlers(ConversationState state) {
        List<ConversationStateHandler> allHandlers = new ArrayList<>();
        if (handlers.containsKey(state)) {
            allHandlers.addAll(handlers.get(state));
        }
        allHandlers.addAll(globalHandlers);
        return allHandlers;
    }
}
