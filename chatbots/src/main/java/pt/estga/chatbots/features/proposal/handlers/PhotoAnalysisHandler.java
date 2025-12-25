package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;
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
        return ProposalState.AWAITING_PHOTO_ANALYSIS;
    }
}
