package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.service.ProposalNavigationService;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubmitPhotoHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final ProposalNavigationService navigationService;
    private final LoopOptionsHandler loopOptionsHandler;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getFileData() == null) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.EXPECTING_PHOTO_ERROR)).build())
                    .build());
        }

        try {
            // Ensure a proposal exists before proceeding
            if (context.getProposal() == null) {
                MarkOccurrenceProposal newProposal = proposalFlowService.startProposal(context.getDomainUserId());
                context.setProposal(newProposal);
            }

            // Add the photo and get the updated proposal
            MarkOccurrenceProposal updatedProposal = proposalFlowService.addPhoto(context.getProposal().getId(), input.getFileData(), input.getFileName());
            context.setProposal(updatedProposal); // Update the context with the fresh proposal
            
            // After adding the photo, let the navigation service decide what's next
            List<BotResponse> responses = navigationService.navigate(context);
            if (responses != null) {
                return responses;
            }

            // If navigation is complete, move to the loop options
            return loopOptionsHandler.handle(context, BotInput.builder().build());

        } catch (IOException e) {
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.ERROR_PROCESSING_PHOTO)).build())
                    .build());
        }
    }

    @Override
    public ConversationState canHandle() {
        // This handler now handles all photo submissions, not just re-uploads
        return ConversationState.WAITING_FOR_PHOTO;
    }
}
