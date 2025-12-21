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
public class ProposeNewMonumentHandler implements ConversationStateHandler {

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {

        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.PROPOSE_NEW_MONUMENT))
            return null;

        context.setCurrentState(ConversationState.AWAITING_NEW_MONUMENT_NAME);

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().title("Please provide the name for the new monument.").build())
                .build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
