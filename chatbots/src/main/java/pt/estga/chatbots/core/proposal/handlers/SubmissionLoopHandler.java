package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.context.ProposalState;
import pt.estga.chatbots.core.shared.models.BotInput;

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
