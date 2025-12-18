package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
@RequiredArgsConstructor
public class ProposeNewMarkHandler implements ConversationStateHandler {

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals("propose_new_mark")) {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please provide the details for the new mark.").build())
                    .build();
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
