package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoAnalysisHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal)) {
            return HandlerOutcome.FAILURE;
        }
        
        MarkOccurrenceProposal markProposal = (MarkOccurrenceProposal) proposal;
        
        // suggestMarks will now handle analysis internally if needed
        List<Mark> suggestedMarks = proposalFlowService.suggestMarks(markProposal);
        
        List<String> suggestedMarkIds = suggestedMarks.stream()
                .map(mark -> mark.getId().toString())
                .collect(Collectors.toList());
        context.getProposalContext().setSuggestedMarkIds(suggestedMarkIds);

        log.info("Photo analysis complete for proposal. Found {} suggestions.", suggestedMarks.size());

        if (suggestedMarks.isEmpty()) {
            markProposal.setNewMark(true);
            markProposal.setExistingMark(null);
        }

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_PHOTO_ANALYSIS;
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
