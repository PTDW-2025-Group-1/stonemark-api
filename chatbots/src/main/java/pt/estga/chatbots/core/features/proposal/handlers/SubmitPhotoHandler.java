package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return null;
        }

        try {
            proposalFlowService.updatePhoto(context.getProposal().getId(), input.getFileData(), input.getFileName());
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return loopOptionsHandler.handle(context, BotInput.builder().build());
        } catch (IOException e) {
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_REUPLOAD_PHOTO;
    }
}
