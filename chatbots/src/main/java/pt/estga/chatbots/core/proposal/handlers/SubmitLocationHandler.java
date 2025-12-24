package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitLocationHandler implements ConversationStateHandler {

    private final ChatbotProposalFlowService proposalFlowService;
    private final ProposalNavigationService navigationService;
    private final LoopOptionsHandler loopOptionsHandler;
    private final UiTextService textService;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getLocation() == null) {
            log.warn("Received non-location input when awaiting location for proposal ID: {}", context.getProposal().getId());
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.EXPECTING_LOCATION_ERROR)).build())
                    .build());
        }

        log.info("Handling location submission for proposal ID: {}", context.getProposal().getId());
        
        // Add the location and get the updated proposal
        MarkOccurrenceProposal updatedProposal = proposalFlowService.addLocation(
                context.getProposal().getId(),
                input.getLocation().getLatitude(),
                input.getLocation().getLongitude()
        );
        context.setProposal(updatedProposal); // Update the context with the fresh proposal

        // After adding the location, let the navigation service decide what's next
        List<BotResponse> responses = navigationService.navigate(context);
        if (responses != null) {
            return responses;
        }

        // If navigation is complete, move to the loop options
        return loopOptionsHandler.handle(context, BotInput.builder().build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_LOCATION;
    }
}
