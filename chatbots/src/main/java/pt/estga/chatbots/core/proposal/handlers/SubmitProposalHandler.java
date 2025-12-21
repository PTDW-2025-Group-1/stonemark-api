package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.handlers.StartHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitProposalHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final StartHandler startHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {

        if (input.getCallbackData() == null || !input.getCallbackData().equals(ProposalCallbackData.SUBMIT_PROPOSAL))
            return null;

        submissionService.submit(context.getProposal().getId());
        context.setProposal(null);
        context.setCurrentState(ConversationState.START);

        input.setText("/start");
        return startHandler.handle(context, input);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.READY_TO_SUBMIT;
    }
}
