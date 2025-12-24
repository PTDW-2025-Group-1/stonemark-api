package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.ContactRequest;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.chatbots.core.verification.VerificationCallbackData;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class StartVerificationHandler implements ConversationStateHandler {

    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getCallbackData() != null && input.getCallbackData().equals(VerificationCallbackData.START_VERIFICATION)) {
            context.setCurrentState(ConversationState.AWAITING_CONTACT);
            
            ContactRequest contactRequest = ContactRequest.builder()
                    .messageNode(textService.get(Messages.SHARE_PHONE_NUMBER_PROMPT))
                    .build();

            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(contactRequest)
                    .build());
        }
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
