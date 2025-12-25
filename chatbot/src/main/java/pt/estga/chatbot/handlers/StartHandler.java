package pt.estga.chatbot.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.constants.Emojis;
import pt.estga.chatbot.constants.Messages;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.text.TextNode;
import pt.estga.chatbot.services.MainMenuFactory;
import pt.estga.chatbot.services.UiTextService;
import pt.estga.user.entities.User;
import pt.estga.user.entities.UserIdentity;
import pt.estga.user.enums.Provider;
import pt.estga.user.services.UserIdentityService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartHandler {

    private final UserIdentityService userIdentityService;
    private final UiTextService textService;
    private final MainMenuFactory mainMenuFactory;

    /**
     * Handles the global /start command. This is not a stateful handler.
     * It generates a welcome message and shows the main options' menu.
     */
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        // 1. Determine the welcome message as a TextNode
        TextNode welcomeMessage;
        try {
            Optional<User> userOptional = userIdentityService
                    .findByProviderAndValue(Provider.valueOf(input.getPlatform()), input.getUserId())
                    .map(UserIdentity::getUser);

            welcomeMessage = userOptional
                    .map(user -> textService.get(Messages.WELCOME_BACK, user.getFirstName(), Emojis.WAVE))
                    .orElse(textService.get(Messages.WELCOME, Emojis.WAVE));
        } catch (Exception e) {
            log.error("Error retrieving user for welcome message", e);
            welcomeMessage = textService.get(Messages.WELCOME, Emojis.WAVE);
        }

        // 2. Build the main options menu using the factory
        var mainMenu = mainMenuFactory.create(input);

        // 3. Combine the welcome message and the menu into a list of responses
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().textNode(welcomeMessage).build());
        responses.add(BotResponse.builder().uiComponent(mainMenu).build());

        return responses;
    }
}
