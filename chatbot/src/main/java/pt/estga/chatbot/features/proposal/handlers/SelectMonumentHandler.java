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
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class SelectMonumentHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();
        
        if (callbackData == null || !callbackData.startsWith(ProposalCallbackData.SELECT_MONUMENT_PREFIX)) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        try {
            Long monumentId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MONUMENT_PREFIX.length()));
            var updatedProposal = proposalFlowService.selectMonument(context.getProposalContext().getProposal().getId(), monumentId);
            context.getProposalContext().setProposal(updatedProposal);
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
