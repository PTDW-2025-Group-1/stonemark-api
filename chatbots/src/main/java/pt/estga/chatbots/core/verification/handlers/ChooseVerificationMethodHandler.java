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
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.chatbots.core.verification.VerificationCallbackData;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ChooseVerificationMethodHandler implements ConversationStateHandler {

    private final TextTemplateParser parser;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        String callbackData = input.getCallbackData();

        if (callbackData == null) {
            return null;
        }

        if (callbackData.equals(VerificationCallbackData.CHOOSE_VERIFY_WITH_CODE)) {
            context.setCurrentState(ConversationState.AWAITING_VERIFICATION_CODE);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse(Messages.ENTER_VERIFICATION_CODE_PROMPT)).build())
                    .build());
        }

        if (callbackData.equals(VerificationCallbackData.CHOOSE_VERIFY_WITH_PHONE)) {
            context.setCurrentState(ConversationState.AWAITING_CONTACT);
            ContactRequest contactRequest = ContactRequest.builder()
                    .messageNode(parser.parse(Messages.SHARE_CONTACT_PROMPT))
                    .build();
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(contactRequest)
                    .build());
        }

        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_VERIFICATION_METHOD;
    }
}
