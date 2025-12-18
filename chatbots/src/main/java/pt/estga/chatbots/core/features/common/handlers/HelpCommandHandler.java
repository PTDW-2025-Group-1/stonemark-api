package pt.estga.chatbots.core.features.common.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
public class HelpCommandHandler implements ConversationStateHandler {
    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/help")) {
            String helpText = "You can start a new submission by sending a photo. " +
                    "Use /start to restart the conversation at any time.";
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title(helpText).build())
                    .build();
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
