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
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final CoordinatesProcessorService coordinatesProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        Long proposalId = context.getProposal().getId();
        String callbackData = input.getCallbackData();
        Long markId = Long.valueOf(callbackData.substring(CallbackData.SELECT_MARK_PREFIX.length()));
        proposalFlowService.selectMark(proposalId, markId);
        
        context.setCurrentState(ConversationState.LOOP_OPTIONS);
        return coordinatesProcessorService.processCoordinates(context);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
