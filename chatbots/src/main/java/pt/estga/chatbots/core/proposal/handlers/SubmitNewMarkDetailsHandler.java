package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.context.ProposalState;
import pt.estga.chatbots.core.shared.models.BotInput;
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
