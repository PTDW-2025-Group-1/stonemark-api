package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.proposal.service.CoordinatesProcessorService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final CoordinatesProcessorService coordinatesProcessorService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        boolean matches = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(input.getCallbackData().split(":")[1]);

        if (matches) {
            proposalFlowService.selectMark(context.getProposal().getId(), null); // This will be improved later
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return coordinatesProcessorService.processCoordinates(context);
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please provide additional details for this new mark.").build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
