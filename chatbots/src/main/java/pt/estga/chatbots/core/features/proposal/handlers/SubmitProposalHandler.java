package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.common.handlers.StartHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

@Component
@RequiredArgsConstructor
public class SubmitProposalHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalSubmissionService submissionService;
    private final StartHandler startHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {

        if (input.getCallbackData() == null || !input.getCallbackData().equals(CallbackData.SUBMIT_PROPOSAL))
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
