package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposals.services.MarkOccurrenceProposalChatbotFlowService;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;
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
        context.clear();

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NOTES;
    }
}
