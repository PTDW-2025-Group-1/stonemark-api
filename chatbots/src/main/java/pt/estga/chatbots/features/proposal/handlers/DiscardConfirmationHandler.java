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
public class DiscardConfirmationHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        switch (callbackData) {
            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED:
                proposalService.delete(context.getProposal());
                context.setProposal(null);
                return HandlerOutcome.DISCARD_CONFIRMED;
            
            // This callback is used by the "No, go back" button
            case ProposalCallbackData.SUBMISSION_LOOP_OPTIONS:
                return HandlerOutcome.SUCCESS; // Go back to the previous step

            default:
                return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_DISCARD_CONFIRMATION;
    }
}
