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
import pt.estga.proposals.services.ChatbotProposalFlowService;

@Component
@RequiredArgsConstructor
public class SelectMonumentHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        
        if (callbackData == null || !callbackData.startsWith(ProposalCallbackData.SELECT_MONUMENT_PREFIX)) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        try {
            Long monumentId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MONUMENT_PREFIX.length()));
            var updatedProposal = proposalFlowService.selectMonument(context.getProposal().getId(), monumentId);
            context.setProposal(updatedProposal);
            return HandlerOutcome.SUCCESS;
        } catch (NumberFormatException e) {
            return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MONUMENT_SELECTION;
    }
}
