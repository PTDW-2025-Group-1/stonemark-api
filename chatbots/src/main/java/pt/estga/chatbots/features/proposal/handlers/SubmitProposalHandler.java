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
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class SubmitProposalHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {

        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SUBMIT_PROPOSAL)) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        submissionService.submit(context.getProposal().getId());
        
        // Clean up the context for the next conversation
        context.setProposal(null);
        context.setSuggestedMarkIds(null);
        context.setSuggestedMonumentIds(null);

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.READY_TO_SUBMIT;
    }
}
