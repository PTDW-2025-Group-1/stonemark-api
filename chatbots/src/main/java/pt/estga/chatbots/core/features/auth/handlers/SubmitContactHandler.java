package pt.estga.chatbots.core.features.auth.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.user.services.UserContactService;

@Component
@RequiredArgsConstructor
public class SubmitContactHandler implements ConversationStateHandler {

    private final UserContactService userContactService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        String phoneNumber = input.getText();

        return userContactService.findByValue(phoneNumber)
                .map(user -> {
                    context.setDomainUserId(user.getId());
                    context.setCurrentState(ConversationState.AUTHENTICATED);
                    return BotResponse.builder()
                            .uiComponent(Menu.builder().title("Your account is verified!").build())
                            .build();
                })
                .orElseGet(() -> BotResponse.builder()
                        .uiComponent(Menu.builder().title("Could not find an account with this phone number.").build())
                        .build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_CONTACT;
    }
}
