package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.verification.VerificationCallbackData;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StartVerificationHandler implements ConversationStateHandler {

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(VerificationCallbackData.START_VERIFICATION)) {
            context.setCurrentState(ConversationState.AWAITING_VERIFICATION_CODE);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please enter the verification code from the website.").build())
                    .build());
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
