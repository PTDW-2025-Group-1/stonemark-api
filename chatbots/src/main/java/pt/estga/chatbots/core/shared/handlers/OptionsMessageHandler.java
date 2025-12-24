package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.services.AuthService;
import pt.estga.chatbots.core.shared.services.AuthServiceFactory;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.chatbots.core.verification.VerificationCallbackData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OptionsMessageHandler {

    private final AuthServiceFactory authServiceFactory;
    private final TextTemplateParser parser;

    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        return handle(context, input, null);
    }

    public List<BotResponse> handle(ConversationContext context, BotInput input, String customMessage) {
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        boolean isAuthenticated = authService.isAuthenticated(input.getUserId());

        List<Button> buttons = new ArrayList<>();
        String title;

        if (customMessage != null) {
            title = customMessage;
        } else if (isAuthenticated) {
            title = "What would you like to do?";
        } else {
            title = "To use this chatbot, you need to verify your account.";
        }

        if (isAuthenticated) {
            buttons.add(Button.builder().textNode(parser.parse("Propose a mason's mark")).callbackData(ProposalCallbackData.START_SUBMISSION).build());
        } else {
            buttons.add(Button.builder().textNode(parser.parse("Verify Account")).callbackData(VerificationCallbackData.START_VERIFICATION).build());
        }

        Menu mainMenu = Menu.builder()
                .titleNode(parser.parse(title))
                .buttons(List.of(buttons))
                .build();

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(mainMenu)
                .build());
    }
}
