package pt.estga.chatbots.core.features.common.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;

import java.util.List;

@Component
public class StartHandler implements ConversationStateHandler {
    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/start")) {
            context.setCurrentState(ConversationState.START);
            Menu mainMenu = Menu.builder()
                    .title("Welcome! What would you like to do?")
                    .buttons(List.of(
                            List.of(
                                    Button.builder().text("New Submission").callbackData("start_submission").build(),
                                    Button.builder().text("Verify Account").callbackData("start_verification").build()
                            )
                    ))
                    .build();

            return BotResponse.builder()
                    .uiComponent(mainMenu)
                    .build();
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
