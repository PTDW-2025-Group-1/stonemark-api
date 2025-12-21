package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.proposal.service.MonumentSuggestionProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class SelectMarkHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MonumentSuggestionProcessorService monumentSuggestionProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        MarkOccurrenceProposal proposal = context.getProposal();
        String callbackData = input.getCallbackData();
        Long markId = Long.valueOf(callbackData.substring(CallbackData.SELECT_MARK_PREFIX.length()));
        proposal = proposalFlowService.selectMark(proposal.getId(), markId);
        context.setProposal(proposal);

        return monumentSuggestionProcessorService.processMonumentSuggestions(context, proposal);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
