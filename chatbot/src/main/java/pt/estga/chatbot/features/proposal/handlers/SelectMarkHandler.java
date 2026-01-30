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
import pt.estga.content.entities.Mark;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

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

        if (callbackData.startsWith(ProposalCallbackData.SELECT_MARK_PREFIX)) {
            try {
                Long markId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MARK_PREFIX.length()));
                markProposal.setExistingMark(Mark.builder().id(markId).build());
                markProposal.setNewMark(false);
                return HandlerOutcome.SUCCESS;
            } catch (NumberFormatException e) {
                return HandlerOutcome.FAILURE;
            }
        }

        if (callbackData.equals(ProposalCallbackData.PROPOSE_NEW_MARK)) {
            markProposal.setNewMark(true);
            markProposal.setExistingMark(null);
            return HandlerOutcome.PROPOSE_NEW;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MARK_SELECTION;
    }
}
