package pt.estga.chatbot.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.features.verification.VerificationCallbackData;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalService;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class MainMenuHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalService proposalService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        String callbackData = input.getCallbackData();

        if (callbackData.equals(ProposalCallbackData.START_SUBMISSION)) {
            Optional<MarkOccurrenceProposal> existingProposal = proposalService.findIncompleteByUserId(context.getDomainUserId());

            if (existingProposal.isPresent()) {
                context.setProposal(existingProposal.get());
                return HandlerOutcome.SUCCESS;
            }

            return HandlerOutcome.START_NEW;
        }

        if (callbackData.equals(VerificationCallbackData.START_VERIFICATION)) {
            return HandlerOutcome.START_VERIFICATION;
        }
        
        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return CoreState.START;
    }
}
