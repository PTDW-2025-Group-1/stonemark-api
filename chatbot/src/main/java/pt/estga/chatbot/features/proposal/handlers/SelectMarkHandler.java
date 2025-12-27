package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposals.services.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        if (callbackData.startsWith(ProposalCallbackData.SELECT_MARK_PREFIX)) {
            try {
                Long markId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MARK_PREFIX.length()));
                var updatedProposal = proposalFlowService.selectMark(context.getProposal().getId(), markId);
                context.setProposal(updatedProposal);
                return HandlerOutcome.SUCCESS;
            } catch (NumberFormatException e) {
                return HandlerOutcome.FAILURE;
            }
        }

        if (callbackData.equals(ProposalCallbackData.PROPOSE_NEW_MARK)) {
            return HandlerOutcome.PROPOSE_NEW;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MARK_SELECTION;
    }
}
