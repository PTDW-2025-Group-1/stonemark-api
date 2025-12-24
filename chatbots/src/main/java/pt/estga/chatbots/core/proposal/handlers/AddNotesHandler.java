package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.OptionsMessageHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.proposals.services.ChatbotProposalFlowService;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final OptionsMessageHandler optionsMessageHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SKIP_NOTES)) {
            proposalFlowService.addNotes(context.getProposal().getId(), input.getText());
        }

        submissionService.submit(context.getProposal().getId());
        context.setProposal(null);
        context.setCurrentState(ConversationState.START);

        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().text("Thank you for your submission!").build());
        responses.addAll(optionsMessageHandler.handle(context, input));

        return responses;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NOTES;
    }
}
