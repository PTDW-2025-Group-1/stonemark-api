package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.proposal.service.MonumentProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SubmitNewMonumentNameHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MonumentProcessorService monumentProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        var proposal = context.getProposal();
        MarkOccurrenceProposal updatedProposal = proposalFlowService.proposeMonument(
                proposal.getId(),
                input.getText(),
                proposal.getLatitude(),
                proposal.getLongitude()
        );
        context.setCurrentState(ConversationState.LOOP_OPTIONS);
        return monumentProcessorService.processMonumentStep(context, updatedProposal);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NEW_MONUMENT_NAME;
    }
}
