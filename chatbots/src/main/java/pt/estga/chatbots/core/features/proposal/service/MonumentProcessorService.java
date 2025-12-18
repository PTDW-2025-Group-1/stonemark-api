package pt.estga.chatbots.core.features.proposal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MonumentProcessorService {

    private final ObjectMapper objectMapper;
    private final MarkService markService;

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

        context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);

        try {
            if (updatedProposal.getSuggestedMarkIds() == null) {
                log.info("No suggested mark IDs found for proposal ID: {}", updatedProposal.getId());
                context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("No existing marks found. Please enter the details for this new mark.").build())
                        .build();
            }

            List<String> suggestedMarkIds = objectMapper.readValue(updatedProposal.getSuggestedMarkIds(), new TypeReference<>() {});
            if (suggestedMarkIds.isEmpty()) {
                log.info("Suggested mark IDs list is empty for proposal ID: {}", updatedProposal.getId());
                context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("No existing marks found. Please enter the details for this new mark.").build())
                        .build();
            }

            log.info("Found {} suggested marks for proposal ID: {}", suggestedMarkIds.size(), updatedProposal.getId());
            List<List<Button>> markButtons = new ArrayList<>();
            for (String markId : suggestedMarkIds) {
                Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
                markOptional.ifPresent(mark -> {
                    List<Button> row = new ArrayList<>();
                    row.add(Button.builder().text(mark.getTitle()).callbackData("select_mark:" + mark.getId()).build());
                    markButtons.add(row);
                });
            }

            List<Button> proposeNewRow = new ArrayList<>();
            proposeNewRow.add(Button.builder().text("Propose New Mark").callbackData("propose_new_mark").build());
            markButtons.add(proposeNewRow);

            Menu markSelectionMenu = Menu.builder()
                    .title("I found some marks that might match. Please select one or propose a new one:")
                    .buttons(markButtons)
                    .build();

            return BotResponse.builder()
                    .uiComponent(markSelectionMenu)
                    .build();
        } catch (JsonProcessingException e) {
            log.error("Error processing mark suggestions for proposal ID: {}: {}", updatedProposal.getId(), e.getMessage());
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing mark suggestions.").build())
                    .build();
        }
    }
}
