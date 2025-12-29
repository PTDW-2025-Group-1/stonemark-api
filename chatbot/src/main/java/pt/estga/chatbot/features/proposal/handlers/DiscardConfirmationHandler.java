package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.services.MarkOccurrenceProposalService;

@Component
@RequiredArgsConstructor
public class DiscardConfirmationHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        switch (callbackData) {
            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED:
                proposalService.delete(context.getProposalContext().getProposal());
                context.getProposalContext().setProposal(null);
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
