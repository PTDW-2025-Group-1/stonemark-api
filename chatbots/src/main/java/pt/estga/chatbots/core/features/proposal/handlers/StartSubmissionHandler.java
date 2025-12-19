package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
@RequiredArgsConstructor
public class StartSubmissionHandler implements ConversationStateHandler {

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(CallbackData.START_SUBMISSION)) {
            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please send a clear photo of the mark.").build())
                    .build();
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
