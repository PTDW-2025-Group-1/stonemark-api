package pt.estga.chatbots.core.shared.handlers;

import lombok.RequiredArgsConstructor;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OptionsMessageHandler {

    private final AuthServiceFactory authServiceFactory;
    private final UiTextService textService;

    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        return handle(context, input, null);
    }

    public List<BotResponse> handle(ConversationContext context, BotInput input, String customMessageKey) {
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        boolean isAuthenticated = authService.isAuthenticated(input.getUserId());

        List<Button> buttons = new ArrayList<>();
        TextNode titleNode;

        if (customMessageKey != null) {
            titleNode = textService.get(customMessageKey);
        } else if (isAuthenticated) {
            titleNode = textService.get(Messages.LOOP_OPTIONS_TITLE);
        } else {
            titleNode = textService.get(Messages.AUTH_REQUIRED_TITLE);
        }

        if (isAuthenticated) {
            buttons.add(Button.builder().textNode(textService.get(Messages.PROPOSE_MARK_BTN))
                    .callbackData(ProposalCallbackData.START_SUBMISSION).build());
        } else {
            buttons.add(Button.builder().textNode(textService.get(Messages.VERIFY_ACCOUNT_BTN))
                    .callbackData(VerificationCallbackData.START_VERIFICATION).build());
        }

        Menu mainMenu = Menu.builder()
                .titleNode(titleNode)
                .buttons(List.of(buttons))
                .build();

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(mainMenu)
                .build());
    }
}
