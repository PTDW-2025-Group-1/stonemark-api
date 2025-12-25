package pt.estga.chatbots.features.verification.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.ConversationState;
import pt.estga.chatbots.context.ConversationStateHandler;
import pt.estga.chatbots.context.HandlerOutcome;
import pt.estga.chatbots.context.VerificationState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.chatbots.features.verification.VerificationCallbackData;

@Component
@RequiredArgsConstructor
public class ChooseVerificationMethodHandler implements ConversationStateHandler {

    @Override
    public HandlerOutcome handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return HandlerOutcome.AWAITING_INPUT;
        }

        if (callbackData.equals(VerificationCallbackData.CHOOSE_VERIFY_WITH_CODE)) {
            return HandlerOutcome.VERIFY_WITH_CODE;
        }

        if (callbackData.equals(VerificationCallbackData.CHOOSE_VERIFY_WITH_PHONE)) {
            return HandlerOutcome.VERIFY_WITH_PHONE;
        }

        return HandlerOutcome.FAILURE;
    }

    @Override
    public ConversationState canHandle() {
        return VerificationState.AWAITING_VERIFICATION_METHOD;
    }
}
