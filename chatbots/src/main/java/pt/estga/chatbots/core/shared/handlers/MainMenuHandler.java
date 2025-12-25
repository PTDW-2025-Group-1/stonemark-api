package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.CoreState;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.verification.VerificationCallbackData;
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
