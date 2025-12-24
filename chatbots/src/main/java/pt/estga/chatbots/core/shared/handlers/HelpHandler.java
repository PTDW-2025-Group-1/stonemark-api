package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.auth.handlers.AuthenticationGuardHandler;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
@Order(2)
public class HelpHandler implements ConversationStateHandler {

    private final OptionsMessageHandler optionsMessageHandler;
    private final AuthenticationGuardHandler authenticationGuard;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/help")) {
            // Reset state to START so the user isn't stuck
            context.setCurrentState(ConversationState.START);

            if (!authenticationGuard.isAuthenticated(input)) {
                return authenticationGuard.requireVerification(context);
            }

            // Reuse the options menu logic but with a custom title
            return optionsMessageHandler.handle(context, input, Messages.HELP_OPTIONS_TITLE);
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return null; // Global handler
    }
}
