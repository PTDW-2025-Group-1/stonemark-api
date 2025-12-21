package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

@Component
@RequiredArgsConstructor
public class BackToMainMenuHandler implements ConversationStateHandler {

    private final OptionsMessageHandler optionsMessageHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(SharedCallbackData.BACK_TO_MAIN_MENU)) {
            return optionsMessageHandler.handle(context, input);
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return null; // This handler can be called from any state
    }
}
