package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.SendLocationManuallyCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
@RequiredArgsConstructor
public class SendLocationManuallyCommandHandler implements CommandHandler<SendLocationManuallyCommand> {

    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(SendLocationManuallyCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        context.setCurrentStateName("AWAITING_LOCATION");

        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Please send the location.").build())
                .build();
    }
}
