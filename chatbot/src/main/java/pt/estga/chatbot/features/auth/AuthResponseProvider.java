package pt.estga.chatbot.features.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.constants.MessageKey;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.ui.Button;
import pt.estga.chatbot.models.ui.Menu;
import pt.estga.chatbot.services.UiTextService;
import pt.estga.chatbot.features.verification.VerificationCallbackData;

import java.util.Collections;
import java.util.List;

import static pt.estga.chatbot.constants.EmojiKey.CHECK;

@Component
@RequiredArgsConstructor
public class AuthResponseProvider {

    private final UiTextService textService;

    public List<BotResponse> requireVerification(ChatbotContext context) {
        context.setCurrentState(CoreState.START);
        Menu verificationMenu = Menu.builder()
                .titleNode(textService.get(MessageKey.AUTH_REQUIRED_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(MessageKey.VERIFY_ACCOUNT_BTN, CHECK)).callbackData(VerificationCallbackData.START_VERIFICATION).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(verificationMenu).build());
    }
}
