package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.features.proposal.ProposalCallbackData;
import pt.estga.chatbots.SharedCallbackData;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.proposals.services.ChatbotProposalFlowService;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null || !callbackData.startsWith(ProposalCallbackData.CONFIRM_MARK_PREFIX)) {
            return (callbackData == null) ? HandlerOutcome.AWAITING_INPUT : HandlerOutcome.FAILURE;
        }

        String[] callbackParts = callbackData.split(":");
        if (callbackParts.length < 2) {
            return HandlerOutcome.FAILURE;
        }

        boolean matches = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(callbackParts[1]);

        if (matches) {
            if (callbackParts.length < 3) {
                return HandlerOutcome.FAILURE; // Mark ID is missing
            }
            try {
                Long markId = Long.valueOf(callbackParts[2]);
                proposalFlowService.selectMark(context.getProposal().getId(), markId);
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
        return ProposalState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
