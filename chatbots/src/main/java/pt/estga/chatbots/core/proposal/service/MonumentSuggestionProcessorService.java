package pt.estga.chatbots.core.proposal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MonumentService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonumentSuggestionProcessorService {

    private final ObjectMapper objectMapper;
    private final MonumentService monumentService;

    public List<BotResponse> processMonumentSuggestions(ConversationContext context, MarkOccurrenceProposal updatedProposal) {
        context.setCurrentState(ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION);

        try {
            if (updatedProposal.getSuggestedMonumentIds() == null) {
                log.warn("No suggested monument IDs found for proposal ID: {}", updatedProposal.getId());
                return handleNoMonumentsFound(context);
            }
            
            List<String> suggestedMonumentIds = objectMapper.readValue(updatedProposal.getSuggestedMonumentIds(), new TypeReference<>() {});
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
                        .title("Was this photo taken at " + monument.getName() + "?")
                        .buttons(List.of(
                                List.of(
                                        Button.builder().text("✅ Yes").callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + monument.getId()).build(),
                                        Button.builder().text("❌ No").callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                                )
                        ))
                        .build();
                return Collections.singletonList(BotResponse.builder().uiComponent(monumentConfirmationMenu).build());
            } else {
                log.error("Monument with ID {} not found for proposal ID: {}", suggestedMonumentIds.getFirst(), updatedProposal.getId());
                return Collections.singletonList(BotResponse.builder()
                        .uiComponent(Menu.builder().title("Error processing monument suggestions.").build())
                        .build());
            }
        } catch (JsonProcessingException e) {
            log.error("Error processing monument suggestions for proposal ID: {}: {}", updatedProposal.getId(), e.getMessage());
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing monument suggestions.").build())
                    .build());
        }
    }

    private List<BotResponse> handleNoMonumentsFound(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_NEW_MONUMENT_NAME);
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().title("No nearby monuments found. Please enter the monument name.").build())
                .build());
    }
}
