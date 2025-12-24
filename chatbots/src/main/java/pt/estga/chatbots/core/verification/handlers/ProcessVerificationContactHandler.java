package pt.estga.chatbots.core.verification.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProcessVerificationContactHandler implements ConversationStateHandler {

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getType() != BotInput.InputType.CONTACT || input.getText() == null) {
            return null;
        }

        String phoneNumber = input.getText();
        log.info("Received phone number for verification: {}", phoneNumber);
        context.setVerificationPhoneNumber(phoneNumber);
        context.setCurrentState(ConversationState.AWAITING_VERIFICATION_CODE);

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().title(Messages.ENTER_CODE_AFTER_CONTACT_PROMPT).build())
                .build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_CONTACT;
    }
}
