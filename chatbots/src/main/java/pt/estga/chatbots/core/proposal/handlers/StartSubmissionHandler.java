package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StartSubmissionHandler implements ConversationStateHandler {

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.START_SUBMISSION)) {
            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please send a clear photo of the mark.").build())
                    .build());
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
