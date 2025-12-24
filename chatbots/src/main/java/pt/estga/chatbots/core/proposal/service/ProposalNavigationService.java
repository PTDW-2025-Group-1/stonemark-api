package pt.estga.chatbots.core.proposal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.services.UiTextService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProposalNavigationService {

    private final ChatbotProposalFlowService proposalFlowService;
    private final UiTextService textService;

    public List<BotResponse> navigate(ConversationContext context) {
        MarkOccurrenceProposal proposal = context.getProposal();

        // Re-fetch from DB to avoid stale state
        if (proposal != null && proposal.getId() != null) {
            proposal = proposalFlowService.getProposal(proposal.getId());
            context.setProposal(proposal); // Ensure context has the fresh object
        }

        if (proposal == null) {
            // This case should ideally be handled by the StartHandler,
            // but as a safeguard, we ask for a photo.
            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.REQUEST_PHOTO_PROMPT)).build())
                    .build());
        }

        // Check if a photo is missing
        if (proposal.getOriginalMediaFile() == null) {
            context.setCurrentState(ConversationState.WAITING_FOR_PHOTO);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().titleNode(textService.get(Messages.REQUEST_PHOTO_PROMPT)).build())
                    .build());
        }

        // Check if a location is missing
        if (proposal.getLatitude() == null || proposal.getLongitude() == null) {
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(LocationRequest.builder().messageNode(textService.get(Messages.REQUEST_LOCATION_PROMPT)).build())
                    .build());
        }

        // If both are present, proceed to the next major step (e.g., analysis or review)
        context.setCurrentState(ConversationState.LOOP_OPTIONS);
        // Here we can directly build the loop options menu, or delegate to the handler
        // Delegating is better to keep logic separated.
        // For now, let's just indicate the next step. The handler will build the UI.
        // This requires LoopOptionsHandler to be called next.
        return null; // Let the next handler in the chain (LoopOptionsHandler) take over.
    }
}
