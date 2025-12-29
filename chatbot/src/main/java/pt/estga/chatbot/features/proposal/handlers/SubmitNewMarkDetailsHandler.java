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
public class SubmitNewMarkDetailsHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String description;
        if (input.getText() != null) {
            description = input.getText();
        } else if (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.SKIP_MARK_DETAILS)) {
            description = "";
        } else {
            return HandlerOutcome.AWAITING_INPUT;
        }

        var updatedProposal = proposalFlowService.createMark(context.getProposalContext().getProposal().getId(), description);
        context.getProposalContext().setProposal(updatedProposal);

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NEW_MARK_DETAILS;
    }
}
