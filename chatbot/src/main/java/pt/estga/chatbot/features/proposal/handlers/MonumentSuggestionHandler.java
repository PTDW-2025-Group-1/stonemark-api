package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonumentSuggestionHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        List<String> suggestedMonumentIds = proposalFlowService.getSuggestedMonumentIds(context.getProposal().getId());
        context.setSuggestedMonumentIds(suggestedMonumentIds);

        log.info("Found {} suggested monuments for proposal {}", suggestedMonumentIds.size(), context.getProposal().getId());

        return HandlerOutcome.SUCCESS;
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.AWAITING_MONUMENT_SUGGESTIONS;
    }
}
