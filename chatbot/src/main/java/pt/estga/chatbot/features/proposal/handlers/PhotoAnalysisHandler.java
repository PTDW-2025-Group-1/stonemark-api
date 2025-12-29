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
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PhotoAnalysisHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        try {
            MarkOccurrenceProposal updatedProposal = proposalFlowService.analyzePhoto(context.getProposalContext().getProposal().getId());
            context.getProposalContext().setProposal(updatedProposal);

            List<String> suggestedMarkIds = proposalFlowService.getSuggestedMarkIds(updatedProposal.getId());
            context.getProposalContext().setSuggestedMarkIds(suggestedMarkIds);

            log.info("Photo analysis complete for proposal {}. Found {} suggestions.", updatedProposal.getId(), suggestedMarkIds.size());

            if (suggestedMarkIds == null || suggestedMarkIds.isEmpty()) {
                updatedProposal = proposalFlowService.createMark(updatedProposal.getId(), null);
                context.getProposalContext().setProposal(updatedProposal);
            }

            return HandlerOutcome.SUCCESS;
        } catch (IOException e) {
            log.error("Error during photo analysis for proposal {}", context.getProposalContext().getProposal().getId(), e);
            return HandlerOutcome.FAILURE;
        }
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
