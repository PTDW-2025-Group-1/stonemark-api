package pt.estga.chatbots.core.features.proposal.handlers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SelectMonumentHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final ObjectMapper objectMapper;
    private final MarkService markService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        var proposal = context.getProposal();
        String callbackData = input.getCallbackData();
        Long monumentId = Long.valueOf(callbackData.substring(CallbackData.SELECT_MONUMENT_PREFIX.length()));
        MarkOccurrenceProposal updatedProposal = proposalFlowService.selectMonument(proposal.getId(), monumentId);
        context.setProposal(updatedProposal);
        context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);

        try {
            List<String> suggestedMarkIds = objectMapper.readValue(updatedProposal.getSuggestedMarkIds(), new TypeReference<>() {});
            List<List<Button>> markButtons = new ArrayList<>();
            for (String markId : suggestedMarkIds) {
                Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
                markOptional.ifPresent(mark -> {
                    List<Button> row = new ArrayList<>();
                    row.add(Button.builder().text(mark.getTitle()).callbackData(CallbackData.SELECT_MARK_PREFIX + mark.getId()).build());
                    markButtons.add(row);
                });
            }

            List<Button> proposeNewRow = new ArrayList<>();
            proposeNewRow.add(Button.builder().text("Propose New Mark").callbackData(CallbackData.PROPOSE_NEW_MARK).build());
            markButtons.add(proposeNewRow);

            Menu markSelectionMenu = Menu.builder()
                    .title("I found some marks that might match. Please select one or propose a new one:")
                    .buttons(markButtons)
                    .build();

            return BotResponse.builder()
                    .uiComponent(markSelectionMenu)
                    .build();
        } catch (JsonProcessingException e) {
            // Handle exception
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing mark suggestions.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
