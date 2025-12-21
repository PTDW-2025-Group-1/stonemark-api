package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.proposal.service.CoordinatesProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SubmitNewMarkDetailsHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final CoordinatesProcessorService coordinatesProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        MarkOccurrenceProposal proposal = context.getProposal();
        proposal = proposalFlowService.proposeMark(proposal.getId(), input.getText());
        context.setProposal(proposal);

        return coordinatesProcessorService.processCoordinates(context);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NEW_MARK_DETAILS;
    }
}
