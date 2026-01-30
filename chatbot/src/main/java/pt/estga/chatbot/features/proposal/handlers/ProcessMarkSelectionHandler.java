package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.constants.SharedCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.content.entities.Mark;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.chatbot.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class ProcessMarkSelectionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal)) {
            return HandlerOutcome.FAILURE;
        }
        MarkOccurrenceProposal markProposal = (MarkOccurrenceProposal) proposal;

        if (callbackData.startsWith(ProposalCallbackData.PROPOSE_NEW_MARK)) {
            markProposal.setNewMark(true);
            markProposal.setExistingMark(null);
            return HandlerOutcome.SUCCESS;
        }

        if (callbackData.startsWith(ProposalCallbackData.CONFIRM_MARK_PREFIX)) {
            String[] callbackParts = callbackData.split(":");
            if (callbackParts.length < 2) {
                return HandlerOutcome.FAILURE;
            }

            boolean matches = SharedCallbackData.CONFIRM_YES.equalsIgnoreCase(callbackParts[1]);
            boolean rejected = SharedCallbackData.CONFIRM_NO.equalsIgnoreCase(callbackParts[1]);

            if (matches) {
                if (callbackParts.length < 3) {
                    return HandlerOutcome.FAILURE; // Mark ID is missing
                }
                try {
                    Long markId = Long.valueOf(callbackParts[2]);
                    markProposal.setExistingMark(Mark.builder().id(markId).build());
                    markProposal.setNewMark(false);
                    return HandlerOutcome.SUCCESS;
                } catch (NumberFormatException e) {
                    return HandlerOutcome.FAILURE;
                }
            } else if (rejected) {
                markProposal.setNewMark(true);
                markProposal.setExistingMark(null);
                return HandlerOutcome.REJECTED;
            }
        }
        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
