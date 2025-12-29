package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposal.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class SubmitProposalHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {

        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SUBMIT_PROPOSAL)) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        submissionService.submit(context.getProposalContext().getProposal().getId());
        
        // Clean up the context for the next conversation
        context.getProposalContext().setProposal(null);
        context.getProposalContext().setSuggestedMarkIds(null);
        context.getProposalContext().setSuggestedMonumentIds(null);

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.READY_TO_SUBMIT;
    }
}
