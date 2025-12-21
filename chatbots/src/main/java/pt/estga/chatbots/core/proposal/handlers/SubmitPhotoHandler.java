package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return null;
        }

        try {
            proposalFlowService.updatePhoto(context.getProposal().getId(), input.getFileData(), input.getFileName());
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return loopOptionsHandler.handle(context, BotInput.builder().build());
        } catch (IOException e) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_REUPLOAD_PHOTO;
    }
}
