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
public class SubmitNewMarkDetailsHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String description;
        if (input.getText() != null) {
            description = input.getText();
        } else if (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.SKIP_MARK_DETAILS)) {
            description = "";
        } else {
            return HandlerOutcome.AWAITING_INPUT;
        }

        var updatedProposal = proposalFlowService.createMark(context.getProposal().getId(), description);
        context.setProposal(updatedProposal);

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NEW_MARK_DETAILS;
    }
}
