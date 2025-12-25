package pt.estga.chatbots.core.shared.services;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.auth.AuthenticationGuard;
import pt.estga.chatbots.core.auth.handlers.AuthenticationGuardHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.handlers.StartHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotConversationServiceImpl implements BotConversationService {

    private final ConversationDispatcher conversationDispatcher;
    private final Cache<String, ConversationContext> conversationContexts;
    private final AuthenticationGuard authenticationGuard;
    private final AuthenticationGuardHandler authenticationGuardHandler;
    private final StartHandler startHandler;

    @Override
    public List<BotResponse> handleInput(BotInput input) {
        ConversationContext context = conversationContexts.get(input.getUserId(), k -> new ConversationContext());

        // Handle global commands that reset the conversation
        boolean isStartCommand = input.getText() != null && input.getText().startsWith("/start");
        boolean isHelpCommand = input.getText() != null && input.getText().startsWith("/help");
        boolean isOptionsCommand = input.getText() != null && input.getText().startsWith("/options");
        boolean isBackToMenu = input.getCallbackData() != null && input.getCallbackData().equals(SharedCallbackData.BACK_TO_MAIN_MENU);

        if (isStartCommand || isHelpCommand || isOptionsCommand || isBackToMenu) {
            // Reset context and let the StartHandler generate the initial response.
            context.setCurrentState(null);
            context.setProposal(null);
            context.setSuggestedMarkIds(null);
            context.setSuggestedMonumentIds(null);
            return startHandler.handle(context, input);
        }
        
        if (context.getCurrentState() == null) {
            context.setCurrentState(ConversationState.START);
        }

        // Perform authentication check for stateful conversation.
        if (!authenticationGuard.isActionAllowed(input, context.getCurrentState())) {
            return authenticationGuardHandler.requireVerification(context);
        }

        // Dispatch to the state machine for all other interactions.
        return conversationDispatcher.dispatch(context, input);
    }
}
