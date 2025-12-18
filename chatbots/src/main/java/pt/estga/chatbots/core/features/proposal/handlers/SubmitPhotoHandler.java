package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.proposal.service.PhotoProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final PhotoProcessorService photoProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        return photoProcessorService.processPhoto(context, input);
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
