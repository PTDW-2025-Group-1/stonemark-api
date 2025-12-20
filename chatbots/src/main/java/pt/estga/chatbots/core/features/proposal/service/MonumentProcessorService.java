package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonumentProcessorService {

    private final MarkProcessorService markProcessorService;

    public BotResponse processMonumentStep(ConversationContext context, MarkOccurrenceProposal updatedProposal) {
        log.info("Processing monument step for proposal ID: {}", updatedProposal.getId());
        context.setProposal(updatedProposal);

        if (updatedProposal.getExistingMark() != null || updatedProposal.getProposedMark() != null) {
            log.info("Mark already selected or proposed for proposal ID: {}", updatedProposal.getId());
            context.setCurrentState(ConversationState.AWAITING_NOTES);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please add any notes for this proposal.").build())
                    .build();
        }

        return markProcessorService.processMarkSuggestions(context, updatedProposal);
    }
}
