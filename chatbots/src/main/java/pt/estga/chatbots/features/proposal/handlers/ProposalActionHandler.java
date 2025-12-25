package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.features.proposal.ProposalCallbackData;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

@Component
@RequiredArgsConstructor
public class ProposalActionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        if (callbackData.equals(ProposalCallbackData.CONTINUE_PROPOSAL)) {
            // The user wants to continue. The flow will decide where to navigate next.
            return HandlerOutcome.CONTINUE;
        }

        if (callbackData.equals(ProposalCallbackData.DELETE_AND_START_NEW)) {
            proposalService.delete(context.getProposal());
            context.setProposal(null);
            // This outcome signals that the old proposal was discarded and we should start fresh.
            return HandlerOutcome.DISCARD_CONFIRMED;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_PROPOSAL_ACTION;
    }
}
