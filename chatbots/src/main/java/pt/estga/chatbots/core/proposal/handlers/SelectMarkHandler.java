package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.proposal.service.MonumentSuggestionProcessorService;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MonumentSuggestionProcessorService monumentSuggestionProcessorService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        MarkOccurrenceProposal proposal = context.getProposal();
        String callbackData = input.getCallbackData();
        Long markId = Long.valueOf(callbackData.substring(ProposalCallbackData.SELECT_MARK_PREFIX.length()));
        proposal = proposalFlowService.selectMark(proposal.getId(), markId);
        context.setProposal(proposal);

        return monumentSuggestionProcessorService.processMonumentSuggestions(context, proposal);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
