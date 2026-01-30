package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.constants.SharedCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.chatbot.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class ConfirmMonumentHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null || !callbackData.startsWith(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX)) {
            return (callbackData == null) ? HandlerOutcome.AWAITING_INPUT : HandlerOutcome.FAILURE;
        }

        String[] callbackParts = callbackData.split(":");
        if (callbackParts.length < 2) {
            return HandlerOutcome.FAILURE;
        }

        boolean confirmed = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(callbackParts[1]);

        if (confirmed) {
            if (callbackParts.length < 3) {
                return HandlerOutcome.FAILURE; // Monument ID is missing
            }
            try {
                Long monumentId = Long.valueOf(callbackParts[2]);
                Proposal proposal = context.getProposalContext().getProposal();
                if (!(proposal instanceof MarkOccurrenceProposal)) {
                    return HandlerOutcome.FAILURE;
                }
                
                // Instead of calling proposalFlowService.selectMonument, we set it directly on the proposal object
                // because we are now submitting all at once at the end.
                MarkOccurrenceProposal markProposal = (MarkOccurrenceProposal) proposal;
                markProposal.setExistingMonument(Monument.builder().id(monumentId).build());
                markProposal.setMonumentName(null); // Clear any previous name if selecting existing

                return HandlerOutcome.SUCCESS;
            } catch (NumberFormatException e) {
                return HandlerOutcome.FAILURE;
            }
        } else {
            return HandlerOutcome.REJECTED;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
