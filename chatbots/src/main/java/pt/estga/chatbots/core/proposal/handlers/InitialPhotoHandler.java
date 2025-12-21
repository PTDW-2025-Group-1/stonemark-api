package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class InitialPhotoHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return null;
        }

        try {
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(context.getDomainUserId(), input.getFileData(), input.getFileName(), null, null);
            context.setProposal(proposal);
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Thank you. Now, please provide the location of the mark.").build())
                    .build());
        } catch (IOException e) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
