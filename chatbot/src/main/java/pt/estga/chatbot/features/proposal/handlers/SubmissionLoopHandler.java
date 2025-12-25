package pt.estga.chatbot.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.ConversationStateHandler;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;

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
