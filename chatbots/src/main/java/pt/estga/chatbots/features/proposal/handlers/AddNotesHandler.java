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
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        // Handle "skip" or text input for notes
        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SKIP_NOTES)) {
            if (input.getText() != null) {
                proposalFlowService.addNotes(context.getProposal().getId(), input.getText());
            }
        }

        // Submit the proposal
        submissionService.submit(context.getProposal().getId());
        
        // Clean up the context
        context.setProposal(null);
        context.setSuggestedMarkIds(null);
        context.setSuggestedMonumentIds(null);

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NOTES;
    }
}
