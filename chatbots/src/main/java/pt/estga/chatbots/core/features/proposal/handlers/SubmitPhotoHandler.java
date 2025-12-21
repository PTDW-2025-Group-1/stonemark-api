package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.proposal.service.MarkProcessorService;
import pt.estga.chatbots.core.features.proposal.service.PhotoProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final PhotoProcessorService photoProcessorService;
    private final MarkProcessorService markProcessorService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return null;
        }

        try {
            MarkOccurrenceProposal proposal = photoProcessorService.processPhoto(context, input);
            if (context.getCurrentState() == ConversationState.LOOP_OPTIONS) {
                return loopOptionsHandler.handle(context, BotInput.builder().build());
            } else {
                return markProcessorService.processMarkSuggestions(context, proposal);
            }
        } catch (IOException e) {
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
