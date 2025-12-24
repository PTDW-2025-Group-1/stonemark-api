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
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitNewMarkDetailsHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final MonumentSuggestionProcessorService monumentSuggestionProcessorService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        MarkOccurrenceProposal proposal = context.getProposal();
        String description = (input.getCallbackData() != null && input.getCallbackData().equals(ProposalCallbackData.SKIP_MARK_DETAILS))
                ? ""
                : input.getText();

        proposal = proposalFlowService.createMark(proposal.getId(), description);
        context.setProposal(proposal);

        return monumentSuggestionProcessorService.processMonumentSuggestions(context, proposal);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NEW_MARK_DETAILS;
    }
}
