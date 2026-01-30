package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class SubmitNewMonumentNameHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        if (input.getText() == null || input.getText().isBlank()) {
            return HandlerOutcome.FAILURE;
        }

        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal)) {
            return HandlerOutcome.FAILURE;
        }

        proposalFlowService.setNewMonumentName(
                (MarkOccurrenceProposal) proposal,
                input.getText()
        );
        
        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NEW_MONUMENT_NAME;
    }
}
