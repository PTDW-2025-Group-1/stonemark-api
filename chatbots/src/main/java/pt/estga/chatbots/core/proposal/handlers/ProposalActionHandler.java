package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotInput;
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
        return ConversationState.AWAITING_PROPOSAL_ACTION;
    }
}
