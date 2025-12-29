package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class InitialLocationHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        if (input.getLocation() == null) {
            // The input was invalid for this handler. Report failure.
            return HandlerOutcome.FAILURE;
        }

        proposalFlowService.addLocation(
                context.getProposalContext().getProposal().getId(),
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
