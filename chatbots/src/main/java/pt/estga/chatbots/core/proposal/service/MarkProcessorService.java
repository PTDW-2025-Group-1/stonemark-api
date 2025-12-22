package pt.estga.chatbots.core.proposal.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.models.ui.PhotoGallery;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarkProcessorService {

    private final MarkService markService;
    private final MarkOccurrenceProposalFlowService proposalFlowService;

    public List<BotResponse> processMarkSuggestions(ConversationContext context, MarkOccurrenceProposal proposal) {
        List<String> suggestedMarkIds = proposalFlowService.getSuggestedMarkIds(proposal.getId());

        if (suggestedMarkIds.isEmpty()) {
            log.info("No suggested mark IDs found for proposal ID: {}", proposal.getId());
            return handleNoMarksFound(context);
        }

        log.info("Found {} suggested marks for proposal ID: {}", suggestedMarkIds.size(), proposal.getId());
        context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);

        List<PhotoGallery.PhotoItem> photoItems = new ArrayList<>();
        for (String markId : suggestedMarkIds) {
            Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));
            markOptional.ifPresent(mark -> {
                Long mediaId = (mark.getCover() != null) ? mark.getCover().getId() : null;
                if (mediaId == null) {
                    log.warn("Mark {} has no cover loaded", mark.getId());
                }

                // Todo: use a better caption
                String caption = "Mark " + mark.getId();

                log.info("Processing mark ID: {}. Media ID: {}", mark.getId(), mediaId);

                photoItems.add(PhotoGallery.PhotoItem.builder()
                        .mediaFileId(mediaId)
                        .caption(caption)
                        .callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId())
                        .build());
            });
        }

        List<Button> proposeNewRow = new ArrayList<>();
        proposeNewRow.add(Button.builder().text("Propose New Mark").callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build());

        PhotoGallery gallery = PhotoGallery.builder()
                .title("I found some marks that might match. Please select one or propose a new one:")
                .photos(photoItems)
                .additionalButtons(List.of(proposeNewRow))
                .build();

        return Collections.singletonList(BotResponse.builder()
                .uiComponent(gallery)
                .build());
    }

    private List<BotResponse> handleNoMarksFound(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_NEW_MARK_DETAILS);
        Menu menu = Menu.builder()
                .title("No existing marks found. Please enter the details for this new mark, or skip.")
                .buttons(List.of(
                        List.of(Button.builder().text("Skip").callbackData(ProposalCallbackData.SKIP_MARK_DETAILS).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(menu)
                .build());
    }
}
