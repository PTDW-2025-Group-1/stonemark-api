package pt.estga.chatbots.core.features.auth.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.features.auth.AuthService;
import pt.estga.chatbots.core.features.auth.commands.SubmitContactCommand;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.user.entities.User;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SubmitContactCommandHandler implements CommandHandler<SubmitContactCommand> {

    private final AuthService authService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(SubmitContactCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        Optional<User> userOptional = authService.authenticate(command.getInput().getUserId(), command.getInput().getText());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            context.setDomainUserId(user.getId());
            context.setCurrentStateName("AUTHENTICATED");
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Welcome, " + user.getFirstName() + "! You are now authenticated.").build())
                    .build();
        } else {
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Authentication failed. Please try again.").build())
                    .build();
        }
    }
}
