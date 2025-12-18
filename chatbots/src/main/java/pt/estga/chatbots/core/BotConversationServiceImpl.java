package pt.estga.chatbots.core;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.infrastructure.Command;
import pt.estga.chatbots.core.infrastructure.CommandFactory;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BotConversationServiceImpl implements BotConversationService {

    private final CommandFactory commandFactory;
    private final ApplicationContext applicationContext;
    private final Cache<String, ConversationContext> conversationContexts;
    private final Map<Class<? extends Command>, CommandHandler<? extends Command>> commandHandlers = new HashMap<>();

    @PostConstruct
    public void init() {
        String[] handlerNames = applicationContext.getBeanNamesForType(CommandHandler.class);
        for (String beanName : handlerNames) {
            CommandHandler<?> handler = (CommandHandler<?>) applicationContext.getBean(beanName);
            ResolvableType resolvableType = ResolvableType.forClass(handler.getClass()).as(CommandHandler.class);
            Class<? extends Command> commandType = (Class<? extends Command>) resolvableType.getGeneric(0).resolve();
            if (commandType != null) {
                commandHandlers.put(commandType, handler);
            }
        }
    }

    @Override
    public BotResponse handleInput(BotInput input) {
        ConversationContext context = conversationContexts.get(input.getUserId(), k -> new ConversationContext());
        Command command = commandFactory.createCommand(input, context);

        @SuppressWarnings("unchecked")
        CommandHandler<Command> handler = (CommandHandler<Command>) commandHandlers.get(command.getClass());

        if (handler != null) {
            return handler.handle(command);
        }

        // Fallback if no handler is found for the created command
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Sorry, I encountered an internal error. Please try /help.").build())
                .build();
    }
}
