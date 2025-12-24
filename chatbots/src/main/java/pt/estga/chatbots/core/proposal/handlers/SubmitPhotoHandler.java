package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;
    private final TextTemplateParser parser;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse("I was expecting a photo. Please upload an image to continue.")).build())
                    .build());
        }

        try {
            proposalFlowService.addPhoto(context.getProposal().getId(), input.getFileData(), input.getFileName());
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return loopOptionsHandler.handle(context, BotInput.builder().build());
        } catch (IOException e) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(parser.parse("Error processing photo. Please try again.")).build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_REUPLOAD_PHOTO;
    }
}
