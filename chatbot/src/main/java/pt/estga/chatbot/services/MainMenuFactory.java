package pt.estga.chatbot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.constants.MessageKey;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.ui.Button;
import pt.estga.chatbot.models.ui.Menu;
import pt.estga.chatbot.features.verification.VerificationCallbackData;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class MainMenuFactory {

    private final AuthServiceFactory authServiceFactory;
    private final UiTextService textService;

    public Menu create(BotInput input) {
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        boolean isAuthenticated = authService.isAuthenticated(input.getUserId());

        List<Button> buttons = new ArrayList<>();
        if (isAuthenticated) {
            buttons.add(Button.builder().textNode(textService.get(MessageKey.PROPOSE_MARK_BTN))
                    .callbackData(ProposalCallbackData.START_SUBMISSION).build());
        } else {
            buttons.add(Button.builder().textNode(textService.get(MessageKey.VERIFY_ACCOUNT_BTN))
                    .callbackData(VerificationCallbackData.START_VERIFICATION).build());
        }

        return Menu.builder()
                .titleNode(textService.get(MessageKey.HELP_OPTIONS_TITLE))
                .buttons(List.of(buttons))
                .build();
    }
}
