package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ProposeNewMarkHandler implements ConversationStateHandler {

    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.PROPOSE_NEW_MARK)) {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.PROVIDE_NEW_MARK_DETAILS_PROMPT)).build())
                    .build());
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
