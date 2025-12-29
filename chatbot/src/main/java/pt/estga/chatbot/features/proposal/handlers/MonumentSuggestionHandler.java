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
import pt.estga.proposal.services.MarkOccurrenceProposalChatbotFlowService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonumentSuggestionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        List<String> suggestedMonumentIds = proposalFlowService.getSuggestedMonumentIds(context.getProposalContext().getProposal().getId());
        context.getProposalContext().setSuggestedMonumentIds(suggestedMonumentIds);

        log.info("Found {} suggested monuments for proposal {}", suggestedMonumentIds.size(), context.getProposalContext().getProposal().getId());

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MONUMENT_SUGGESTIONS;
    }

    @Override
    public boolean isAutomatic() {
        return true;
    }
}
