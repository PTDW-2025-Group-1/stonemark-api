package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.*;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;
import pt.estga.proposal.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal)) {
            return HandlerOutcome.FAILURE;
        }
        MarkOccurrenceProposal markProposal = (MarkOccurrenceProposal) proposal;

        // Handle "skip" or text input for notes
        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SKIP_NOTES)) {
            if (input.getText() != null) {
                proposalFlowService.addNotes(markProposal, input.getText());
            }
        }

        // Submit the proposal
        submissionService.submit(markProposal);
        
        // Clean up the context
        context.clear();

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_NOTES;
    }
}
