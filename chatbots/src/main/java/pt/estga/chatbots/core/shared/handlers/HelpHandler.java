package pt.estga.chatbots.core.shared.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;

import java.util.Collections;
import java.util.List;

@Component
public class HelpHandler implements ConversationStateHandler {
    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getText() != null && input.getText().startsWith("/help")) {
            String helpText = "You can start a new submission by sending a photo. " +
                    "Use /start to restart the conversation at any time.";
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title(helpText).build())
                    .build());
        }
        return null; // Not handled
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.START;
    }
}
