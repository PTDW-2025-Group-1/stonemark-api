package pt.estga.chatbots.core.features.proposal.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.chatbots.core.models.ui.PhotoGallery;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarkProcessorService {

    private final ObjectMapper objectMapper;
    private final MarkService markService;

    @Value("${application.base-url}")
    private String baseUrl;

    @Value("${application.placeholder-image-url:https://placehold.co/600x400.png}")
    private String placeholderImageUrl;

    public BotResponse processMarkSuggestions(ConversationContext context, MarkOccurrenceProposal proposal) {
        try {
            if (proposal.getSuggestedMarkIds() == null) {
                log.info("No suggested mark IDs found for proposal ID: {}", proposal.getId());
                return handleNoMarksFound(context);
            }

            List<String> suggestedMarkIds = objectMapper.readValue(proposal.getSuggestedMarkIds(), new TypeReference<>() {});
            if (suggestedMarkIds.isEmpty()) {
                log.info("Suggested mark IDs list is empty for proposal ID: {}", proposal.getId());
                return handleNoMarksFound(context);
            }

            log.info("Found {} suggested marks for proposal ID: {}", suggestedMarkIds.size(), proposal.getId());
            context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);

            List<PhotoGallery.PhotoItem> photoItems = new ArrayList<>();
            for (String markId : suggestedMarkIds) {
                Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));
                markOptional.ifPresent(mark -> {
                    String imagePath = null;
                    if (mark.getCover() != null) {
                        imagePath = mark.getCover().getStoragePath();
                        if (imagePath == null) {
                            log.warn("Mark {} has cover but storagePath is null", mark.getId());
                        }
                    } else {
                        log.warn("Mark {} has no cover loaded", mark.getId());
                    }

                    String title = mark.getTitle() != null ? mark.getTitle() : "Untitled Mark";
                    
                    String imageUrl = (imagePath != null) ? baseUrl + "/api/v1/media/" + imagePath : placeholderImageUrl;
                    log.info("Processing mark ID: {}. Image path: {}, Final URL: {}", mark.getId(), imagePath, imageUrl);

                    photoItems.add(PhotoGallery.PhotoItem.builder()
                            .imageUrl(imageUrl)
                            .caption(title)
                            .callbackData(CallbackData.SELECT_MARK_PREFIX + mark.getId())
                            .build());
                });
            }

            List<Button> proposeNewRow = new ArrayList<>();
            proposeNewRow.add(Button.builder().text("Propose New Mark").callbackData(CallbackData.PROPOSE_NEW_MARK).build());

            PhotoGallery gallery = PhotoGallery.builder()
                    .title("I found some marks that might match. Please select one or propose a new one:")
                    .photos(photoItems)
                    .additionalButtons(List.of(proposeNewRow))
                    .build();

            return BotResponse.builder()
                    .uiComponent(gallery)
                    .build();

        } catch (JsonProcessingException e) {
            log.error("Error processing mark suggestions for proposal ID: {}: {}", proposal.getId(), e.getMessage());
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Error processing mark suggestions.").build())
                    .build();
        }
    }

    private BotResponse handleNoMarksFound(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("No existing marks found. Please enter the details for this new mark.").build())
                .build();
    }
}
