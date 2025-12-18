package pt.estga.chatbots.core;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.context.ConversationStateHandlerProvider;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationRouter {

    private final ConversationStateHandlerProvider handlerProvider;
    private final Cache<String, ConversationContext> conversationContexts;

    public BotResponse route(BotInput input) {
        ConversationContext context = conversationContexts.get(input.getUserId(), k -> new ConversationContext());
        ConversationState currentState = context.getCurrentState() == null ? ConversationState.START : context.getCurrentState();
        List<ConversationStateHandler> handlers = handlerProvider.getHandlers(currentState);

        if (handlers != null) {
            for (ConversationStateHandler handler : handlers) {
                BotResponse response = handler.handle(context, input);
                if (response != null) {
                    return response;
                }
            }
        }

        // Fallback if no handler is found for the current state
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Sorry, I can't understand you in this context. Please try /help.").build())
                .build();
    }
}
