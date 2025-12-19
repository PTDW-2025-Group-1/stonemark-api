package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.proposal.service.CoordinatesProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final CoordinatesProcessorService coordinatesProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        boolean matches = CallbackData.CONFIRM_YES.equalsIgnoreCase(input.getCallbackData().split(":")[1]);

        if (matches) {
            proposalFlowService.selectMark(context.getProposal().getId(), null); // This will be improved later
            return coordinatesProcessorService.processCoordinates(context);
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please provide additional details for this new mark.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
