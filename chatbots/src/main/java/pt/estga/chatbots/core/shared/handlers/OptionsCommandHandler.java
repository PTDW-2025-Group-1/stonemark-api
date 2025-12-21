package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.features.auth.handlers.AuthenticationGuardHandler;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OptionsCommandHandler implements ConversationStateHandler {

    private final OptionsMessageHandler optionsMessageHandler;
    private final AuthenticationGuardHandler authenticationGuard;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/options")) {
            context.setCurrentState(ConversationState.START);

            if (!authenticationGuard.isAuthenticated(input)) {
                return authenticationGuard.requireVerification(context);
            }

            return optionsMessageHandler.handle(context, input);
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
