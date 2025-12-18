package pt.estga.chatbots.core.features.auth.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.ContactRequest;

@Component
@RequiredArgsConstructor
public class StartAuthenticationHandler implements ConversationStateHandler {

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        // Todo: implement authentication flow
        if (input.getCallbackData() != null && input.getCallbackData().equals("start_verification")) {
            context.setCurrentState(ConversationState.AWAITING_CONTACT);

            ContactRequest contactRequest = ContactRequest.builder()
                    .message("To get started, please share your phone number so I can authenticate you.")
                    .build();

            return BotResponse.builder()
                    .uiComponent(contactRequest)
                    .build();
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
