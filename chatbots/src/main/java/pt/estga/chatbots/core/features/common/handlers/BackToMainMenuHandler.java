package pt.estga.chatbots.core.features.common.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;

@Component
@RequiredArgsConstructor
public class BackToMainMenuHandler implements ConversationStateHandler {

    private final StartHandler startHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(CallbackData.BACK_TO_MAIN_MENU)) {
            return startHandler.handle(context, BotInput.builder().text("/start").build());
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return null; // This handler can be called from any state
    }
}
