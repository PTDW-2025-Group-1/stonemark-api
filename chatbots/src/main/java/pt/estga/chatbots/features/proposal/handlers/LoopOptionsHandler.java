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
public class LoopOptionsHandler implements ConversationStateHandler {

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        // If there's no specific action, it means we just entered this state.
        // The ResponseFactory will show the options. We just wait for the user's choice.
        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        // Translate the user's choice into a specific outcome for the ProposalFlow to handle.
        switch (callbackData) {
            case ProposalCallbackData.LOOP_REDO_LOCATION:
                return HandlerOutcome.CHANGE_LOCATION;
            case ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD:
                return HandlerOutcome.CHANGE_PHOTO;
            case ProposalCallbackData.LOOP_CONTINUE:
                return HandlerOutcome.CONTINUE;
            default:
                // If the callback is invalid, we fail, which will cause the system
                // to show the options again with an error message.
                return HandlerOutcome.FAILURE;
        }
    }

    @Override
    public ConversationState canHandle() {
        return ProposalState.LOOP_OPTIONS;
    }
}
