package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoAnalysisHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        try {
            MarkOccurrenceProposal updatedProposal = proposalFlowService.analyzePhoto(context.getProposal().getId());
            context.setProposal(updatedProposal);

            List<String> suggestedMarkIds = proposalFlowService.getSuggestedMarkIds(updatedProposal.getId());
            context.setSuggestedMarkIds(suggestedMarkIds);

            log.info("Photo analysis complete for proposal {}. Found {} suggestions.", updatedProposal.getId(), suggestedMarkIds.size());

            return HandlerOutcome.SUCCESS;
        } catch (IOException e) {
            log.error("Error during photo analysis for proposal {}", context.getProposal().getId(), e);
            return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_PHOTO_ANALYSIS;
    }
}
