package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.text.TextNode;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.AuthService;
import pt.estga.chatbots.core.shared.services.AuthServiceFactory;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.chatbots.core.verification.VerificationCallbackData;
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
    private final AuthServiceFactory authServiceFactory;
    private final UiTextService textService;

    /**
     * Handles the global /start command. This is not a stateful handler.
     * It generates a welcome message and shows the main options menu.
     */
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        // 1. Determine the welcome message as a TextNode
        TextNode welcomeMessage;
        try {
            Optional<User> userOptional = userIdentityService
                    .findByProviderAndValue(Provider.valueOf(input.getPlatform()), input.getUserId())
                    .map(UserIdentity::getUser);

            welcomeMessage = userOptional
                    .map(user -> textService.get(Messages.WELCOME_BACK, user.getFirstName()))
                    .orElse(textService.get(Messages.WELCOME));
        } catch (Exception e) {
            log.error("Error retrieving user for welcome message", e);
            welcomeMessage = textService.get(Messages.WELCOME);
        }

        // 2. Build the main options menu
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        boolean isAuthenticated = authService.isAuthenticated(input.getUserId());

        List<Button> buttons = new ArrayList<>();
        if (isAuthenticated) {
            buttons.add(Button.builder().textNode(textService.get(Messages.PROPOSE_MARK_BTN))
                    .callbackData(ProposalCallbackData.START_SUBMISSION).build());
        } else {
            buttons.add(Button.builder().textNode(textService.get(Messages.VERIFY_ACCOUNT_BTN))
                    .callbackData(VerificationCallbackData.START_VERIFICATION).build());
        }

        Menu mainMenu = Menu.builder()
                .titleNode(textService.get(Messages.HELP_OPTIONS_TITLE))
                .buttons(List.of(buttons))
                .build();

        // 3. Combine the welcome message and the menu into a list of responses
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().textNode(welcomeMessage).build());
        responses.add(BotResponse.builder().uiComponent(mainMenu).build());

        return responses;
    }
}
