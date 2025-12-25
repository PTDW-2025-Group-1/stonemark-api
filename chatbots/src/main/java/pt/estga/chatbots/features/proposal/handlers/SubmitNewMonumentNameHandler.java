package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.proposals.services.ChatbotProposalFlowService;

@Component
@RequiredArgsConstructor
public class SubmitNewMonumentNameHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        if (input.getText() == null || input.getText().isBlank()) {
            return HandlerOutcome.FAILURE;
        }

        var updatedProposal = proposalFlowService.createMonument(
                context.getProposal().getId(),
                input.getText()
        );
        context.setProposal(updatedProposal);
        
        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NEW_MONUMENT_NAME;
    }
}
