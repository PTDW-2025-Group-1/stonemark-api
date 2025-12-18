package pt.estga.chatbots.core.features.auth.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.auth.commands.StartAuthenticationCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.ContactRequest;

@Component
@RequiredArgsConstructor
public class StartAuthenticationCommandHandler implements CommandHandler<StartAuthenticationCommand> {

    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(StartAuthenticationCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        context.setCurrentStateName("AWAITING_CONTACT");

        ContactRequest contactRequest = ContactRequest.builder()
                .message("To get started, please share your phone number so I can authenticate you.")
                .build();

        return BotResponse.builder()
                .uiComponent(contactRequest)
                .build();
    }
}
