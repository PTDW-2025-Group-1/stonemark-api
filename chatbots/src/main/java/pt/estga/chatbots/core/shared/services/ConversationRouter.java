package pt.estga.chatbots.core.shared.services;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.auth.AuthenticationGuard;
import pt.estga.chatbots.core.auth.handlers.AuthenticationGuardHandler;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.ConversationStateHandlerProvider;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConversationRouter {

    private final ConversationStateHandlerProvider handlerProvider;
    private final Cache<String, ConversationContext> conversationContexts;
    private final AuthenticationGuard authenticationGuard;
    private final AuthenticationGuardHandler authenticationGuardHandler;
    private final UiTextService textService;

    public List<BotResponse> route(BotInput input) {
        ConversationContext context = conversationContexts.get(input.getUserId(), k -> new ConversationContext());
        ConversationState currentState = context.getCurrentState() == null ? ConversationState.START : context.getCurrentState();

        if (!authenticationGuard.isActionAllowed(input, currentState)) {
            return authenticationGuardHandler.requireVerification(context);
        }

        List<ConversationStateHandler> handlers = handlerProvider.getHandlers(currentState);

        if (handlers != null) {
            for (ConversationStateHandler handler : handlers) {
                List<BotResponse> responses = handler.handle(context, input);
                if (responses != null && !responses.isEmpty()) {
                    return responses;
                }
            }
        }

        // Fallback if no handler is found for the current state
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().titleNode(textService.get(Messages.FALLBACK_UNHANDLED_INPUT)).build())
                .build());
    }
}
