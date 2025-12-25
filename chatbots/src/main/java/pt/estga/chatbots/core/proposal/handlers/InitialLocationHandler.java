package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.context.ProposalState;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.proposals.services.ChatbotProposalFlowService;

@Component
@RequiredArgsConstructor
public class InitialLocationHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        if (input.getLocation() == null) {
            // The input was invalid for this handler. Report failure.
            return HandlerOutcome.FAILURE;
        }

        proposalFlowService.addLocation(
                context.getProposal().getId(),
                input.getLocation().getLatitude(),
                input.getLocation().getLongitude()
        );

        // The location was successfully added. Report success.
        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_LOCATION;
    }
}
