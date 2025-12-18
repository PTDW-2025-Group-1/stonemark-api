package pt.estga.chatbots.core;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;

@Service
@RequiredArgsConstructor
public class BotConversationServiceImpl implements BotConversationService {

    private final ConversationRouter conversationRouter;

    @Override
    public BotResponse handleInput(BotInput input) {
        return conversationRouter.route(input);
    }
}
