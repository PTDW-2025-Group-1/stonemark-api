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
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.services.MarkOccurrenceProposalService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProposalActionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        if (callbackData.equals(ProposalCallbackData.CONTINUE_PROPOSAL)) {
            Optional<MarkOccurrenceProposal> proposal = proposalService.findIncompleteByUserId(Long.valueOf(input.getUserId()));
            if (proposal.isPresent()) {
                context.getProposalContext().setProposalId(proposal.get().getId());
                return HandlerOutcome.CONTINUE;
            }
            return HandlerOutcome.FAILURE;
        }

        if (callbackData.equals(ProposalCallbackData.DELETE_AND_START_NEW)) {
            Long proposalId = context.getProposalContext().getProposalId();
            if (proposalId != null) {
                proposalService.findById(proposalId).ifPresent(proposalService::delete);
            }
            context.getProposalContext().setProposalId(null);
            // This outcome signals that the old proposal was discarded and we should start fresh.
            return HandlerOutcome.DISCARD_CONFIRMED;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_PROPOSAL_ACTION;
    }
}
