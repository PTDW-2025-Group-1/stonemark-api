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
import pt.estga.content.entities.Monument;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;
import pt.estga.proposal.services.chatbot.MarkOccurrenceProposalChatbotFlowService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonumentSuggestionHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalChatbotFlowService proposalFlowService;

    @Override
    public HandlerOutcome handle(ChatbotContext context, BotInput input) {
        Proposal proposal = context.getProposalContext().getProposal();
        if (!(proposal instanceof MarkOccurrenceProposal)) {
            return HandlerOutcome.FAILURE;
        }
        MarkOccurrenceProposal markProposal = (MarkOccurrenceProposal) proposal;

        List<Monument> suggestedMonuments = proposalFlowService.suggestMonuments(markProposal);
        
        List<String> suggestedMonumentIds = suggestedMonuments.stream()
                .map(monument -> monument.getId().toString())
                .collect(Collectors.toList());
        context.getProposalContext().setSuggestedMonumentIds(suggestedMonumentIds);

        log.info("Found {} suggested monuments for proposal", suggestedMonuments.size());

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
