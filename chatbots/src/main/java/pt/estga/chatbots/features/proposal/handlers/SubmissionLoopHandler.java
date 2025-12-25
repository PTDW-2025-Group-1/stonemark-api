package pt.estga.chatbots.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.features.proposal.ProposalCallbackData;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.ProposalState;
import pt.estga.chatbots.models.BotInput;

@Component
@RequiredArgsConstructor
public class SubmissionLoopHandler implements ConversationStateHandler {

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        return switch (callbackData) {
            case ProposalCallbackData.SUBMISSION_LOOP_START_OVER -> HandlerOutcome.DISCARD;
            case ProposalCallbackData.SUBMISSION_LOOP_CONTINUE -> HandlerOutcome.CONTINUE;
            default -> HandlerOutcome.FAILURE;
        };
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.SUBMISSION_LOOP_OPTIONS;
    }
}
