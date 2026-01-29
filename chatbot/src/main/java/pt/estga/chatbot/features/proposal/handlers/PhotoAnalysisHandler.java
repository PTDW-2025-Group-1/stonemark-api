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
        MarkOccurrenceProposal proposal = context.getProposalContext().getProposal();
        
        // suggestMarks will now handle analysis internally if needed
        List<Mark> suggestedMarks = proposalFlowService.suggestMarks(proposal);
        
        List<String> suggestedMarkIds = suggestedMarks.stream()
                .map(mark -> mark.getId().toString())
                .collect(Collectors.toList());
        context.getProposalContext().setSuggestedMarkIds(suggestedMarkIds);

        log.info("Photo analysis complete for proposal. Found {} suggestions.", suggestedMarks.size());

        if (suggestedMarks.isEmpty()) {
            proposalFlowService.indicateNewMark(proposal);
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
