package pt.estga.chatbots.core.shared.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.shared.ConversationRouter;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotConversationServiceImpl implements BotConversationService {

    private final ConversationRouter conversationRouter;

    @Override
    public List<BotResponse> handleInput(BotInput input) {
        return conversationRouter.route(input);
    }
}
