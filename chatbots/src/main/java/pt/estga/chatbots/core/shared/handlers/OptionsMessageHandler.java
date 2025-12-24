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
import pt.estga.chatbots.core.verification.VerificationCallbackData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OptionsMessageHandler {

    private final AuthServiceFactory authServiceFactory;

    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        boolean isAuthenticated = authService.isAuthenticated(input.getUserId());

        List<Button> buttons = new ArrayList<>();
        String title;

        if (isAuthenticated) {
            title = "What would you like to do?";
            buttons.add(Button.builder().text("Propose a mason's mark").callbackData(ProposalCallbackData.START_SUBMISSION).build());
        } else {
            title = "To use this chatbot, you need to verify your account.";
            buttons.add(Button.builder().text("Verify Account").callbackData(VerificationCallbackData.START_VERIFICATION).build());
        }

        Menu mainMenu = Menu.builder()
                .title(title)
                .buttons(List.of(buttons))
                .build();

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(mainMenu)
                .build());
    }
}
