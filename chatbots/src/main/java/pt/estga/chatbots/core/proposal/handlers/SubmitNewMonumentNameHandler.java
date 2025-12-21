package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitNewMonumentNameHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final SubmissionLoopHandler submissionLoopHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        var proposal = context.getProposal();
        MarkOccurrenceProposal updatedProposal = proposalFlowService.proposeMonument(
                proposal.getId(),
                input.getText(),
                proposal.getLatitude(),
                proposal.getLongitude()
        );
        context.setProposal(updatedProposal);
        context.setCurrentState(ConversationState.SUBMISSION_LOOP_OPTIONS);
        return submissionLoopHandler.handle(context, BotInput.builder().build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NEW_MONUMENT_NAME;
    }
}
