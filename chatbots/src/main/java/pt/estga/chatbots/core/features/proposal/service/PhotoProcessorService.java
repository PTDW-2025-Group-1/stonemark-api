package pt.estga.chatbots.core.features.proposal.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PhotoProcessorService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkService markService;
    private final ObjectMapper objectMapper;

    public BotResponse processPhoto(ConversationContext context, BotInput input) {
        try {
            Long domainUserId = context.getDomainUserId();
            log.info("Initiating proposal for user ID: {} (domain ID: {}) with file: {}", input.getUserId(), domainUserId, input.getFileName());
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(domainUserId, input.getFileData(), input.getFileName());
            context.setProposal(proposal);
            log.info("Proposal with ID {} created.", proposal.getId());

            if (proposal.getSuggestedMarkIds() != null && !proposal.getSuggestedMarkIds().isEmpty()) {
                context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);
                List<String> suggestedMarkIds = objectMapper.readValue(proposal.getSuggestedMarkIds(), new TypeReference<>() {});
                List<List<Button>> markButtons = new ArrayList<>();
                for (String markId : suggestedMarkIds) {
                    Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
                    markOptional.ifPresent(mark -> {
                        List<Button> row = new ArrayList<>();
                        String buttonText = mark.getTitle() != null ? mark.getTitle() : "Titless Mark";
                        row.add(Button.builder().text(buttonText).callbackData(CallbackData.SELECT_MARK_PREFIX + mark.getId()).build());
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
            } else {
                context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("No existing marks found. Please enter the details for this new mark.").build())
                        .build();
            }

        } catch (IOException e) {
            log.error("Error processing photo for user: {}", input.getUserId(), e);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing photo.").build())
                    .build();
        }
    }
}
