package pt.estga.chatbot.features.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.constants.MessageKey;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.Message;
import pt.estga.chatbot.models.ui.Menu;
import pt.estga.chatbot.services.ResponseProvider;
import pt.estga.chatbot.services.UiTextService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static pt.estga.chatbot.constants.EmojiKey.WAVE;
import static pt.estga.chatbot.constants.EmojiKey.WARNING;

@Component
@RequiredArgsConstructor
public class CoreResponseProvider implements ResponseProvider {

    private final UiTextService textService;
    private final MainMenuFactory mainMenuFactory;

    @Override
    public boolean supports(ConversationState state) {
        return state instanceof CoreState;
    }

    @Override
    public List<BotResponse> createResponse(ChatbotContext context, HandlerOutcome outcome, BotInput input) {
        CoreState state = (CoreState) context.getCurrentState();
        return switch (state) {
            case MAIN_MENU -> {
                List<BotResponse> responses = new ArrayList<>();
                Message welcomeMessage;
                if (context.getUserName() != null) {
                    welcomeMessage = new Message(MessageKey.WELCOME_BACK, context.getUserName(), WAVE);
                } else {
                    welcomeMessage = new Message(MessageKey.WELCOME, WAVE);
                }
                responses.add(BotResponse.builder().textNode(textService.get(welcomeMessage)).build());
                responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
                yield responses;
            }
            default -> {
                // For other core states, if any, we can just return a simple menu or empty list
                yield Collections.emptyList();
            }
        };
    }
    
    private List<BotResponse> buildSimpleMenuResponse(Message message) {
        if (message == null) {
            return Collections.singletonList(BotResponse.builder().textNode(textService.get(new Message(MessageKey.ERROR_GENERIC, WARNING))).build());
        }
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().titleNode(textService.get(message)).build())
                .build());
    }
}
