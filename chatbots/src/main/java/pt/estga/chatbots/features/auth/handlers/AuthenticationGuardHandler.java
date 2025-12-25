package pt.estga.chatbots.features.auth.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.Messages;
import pt.estga.chatbots.context.ConversationContext;
import pt.estga.chatbots.context.CoreState;
import pt.estga.chatbots.models.BotInput;
import pt.estga.chatbots.models.BotResponse;
import pt.estga.chatbots.models.ui.Button;
import pt.estga.chatbots.models.ui.Menu;
import pt.estga.chatbots.services.AuthService;
import pt.estga.chatbots.services.AuthServiceFactory;
import pt.estga.chatbots.services.UiTextService;
import pt.estga.chatbots.features.verification.VerificationCallbackData;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class AuthenticationGuardHandler {

    private final AuthServiceFactory authServiceFactory;
    private final UiTextService textService;

    public boolean isAuthenticated(BotInput input) {
        AuthService authService = authServiceFactory.getAuthService(input.getPlatform());
        return authService.isAuthenticated(input.getUserId());
    }

    public List<BotResponse> requireVerification(ConversationContext context) {
        context.setCurrentState(CoreState.START);
        Menu verificationMenu = Menu.builder()
                .titleNode(textService.get(Messages.AUTH_REQUIRED_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.VERIFY_ACCOUNT_BTN)).callbackData(VerificationCallbackData.START_VERIFICATION).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(verificationMenu).build());
    }
}
