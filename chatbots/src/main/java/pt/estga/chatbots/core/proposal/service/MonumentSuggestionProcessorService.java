package pt.estga.chatbots.core.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonumentSuggestionProcessorService {

    private final MonumentService monumentService;
    private final ChatbotProposalFlowService proposalFlowService;

    public List<BotResponse> processMonumentSuggestions(ConversationContext context, MarkOccurrenceProposal updatedProposal) {
        context.setCurrentState(ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION);

        List<String> suggestedMonumentIds = proposalFlowService.getSuggestedMonumentIds(updatedProposal.getId());

        if (suggestedMonumentIds.isEmpty()) {
            log.info("No suggested monuments found for proposal ID: {}", updatedProposal.getId());
            return handleNoMonumentsFound(context);
        }

        log.info("Found {} suggested monuments for proposal ID: {}", suggestedMonumentIds.size(), updatedProposal.getId());
        Optional<Monument> monumentOptional = monumentService.findById(Long.valueOf(suggestedMonumentIds.getFirst()));
        if (monumentOptional.isPresent()) {
            Monument monument = monumentOptional.get();
            log.info("Suggesting monument '{}' for proposal ID: {}", monument.getName(), updatedProposal.getId());
            Menu monumentConfirmationMenu = Menu.builder()
                    .title(String.format(Messages.MONUMENT_CONFIRMATION_TITLE, monument.getName()))
                    .buttons(List.of(
                            List.of(
                                    Button.builder().text(Messages.YES_BTN).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + monument.getId()).build(),
                                    Button.builder().text(Messages.NO_BTN).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                            )
                    ))
                    .build();
            return Collections.singletonList(BotResponse.builder().uiComponent(monumentConfirmationMenu).build());
        } else {
            log.error("Monument with ID {} not found for proposal ID: {}", suggestedMonumentIds.getFirst(), updatedProposal.getId());
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title(Messages.ERROR_GENERIC).build())
                    .build());
        }
    }

    private List<BotResponse> handleNoMonumentsFound(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_NEW_MONUMENT_NAME);
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().title(Messages.NO_MONUMENTS_FOUND_PROMPT).build())
                .build());
    }
}
