package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialPhotoHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.EXPECTING_PHOTO_ERROR)).build())
                    .build());
        }

        try {
            MarkOccurrenceProposal proposal = proposalFlowService.startProposal(context.getDomainUserId());
            proposal = proposalFlowService.addPhoto(proposal.getId(), input.getFileData(), input.getFileName());
            context.setProposal(proposal);
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(LocationRequest.builder().messageNode(textService.get(Messages.LOCATION_REQUEST_MESSAGE)).build())
                    .build());
        } catch (IOException e) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.ERROR_PROCESSING_PHOTO)).build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
