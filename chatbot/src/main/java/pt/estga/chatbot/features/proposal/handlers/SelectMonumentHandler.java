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
import pt.estga.proposal.entities.Proposal;

import java.util.Collections;

@Component
@RequiredArgsConstructor
public class SelectMonumentHandler implements ConversationStateHandler {

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal markProposal)) {
            return HandlerOutcome.FAILURE;
        }

        if (callbackData.startsWith(ProposalCallbackData.SELECT_MONUMENT_PREFIX)) {
            try {
                long monumentId = Long.parseLong(callbackData.substring(ProposalCallbackData.SELECT_MONUMENT_PREFIX.length()));
                // Set the selected monument as the suggestion so the next state (WAITING_FOR_MONUMENT_CONFIRMATION) can use it
                context.getProposalContext().setSuggestedMonumentIds(Collections.singletonList(Long.toString(monumentId)));
                return HandlerOutcome.SUCCESS;
            } catch (NumberFormatException e) {
                return HandlerOutcome.FAILURE;
            }
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MONUMENT_SELECTION;
    }
}
