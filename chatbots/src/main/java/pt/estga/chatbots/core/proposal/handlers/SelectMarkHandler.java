package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.proposals.services.ChatbotProposalFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

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
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
