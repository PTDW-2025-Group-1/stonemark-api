package pt.estga.chatbots.core.proposal.service;

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
import pt.estga.chatbots.core.shared.models.ui.PhotoItem;
import pt.estga.chatbots.core.shared.models.ui.TextMessage;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.ChatbotProposalFlowService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MarkProcessorService implements SingleMarkMatchProcessor, MultipleMarkMatchProcessor {

    private final MarkService markService;
    private final ChatbotProposalFlowService proposalFlowService;

    public List<BotResponse> processMarkSuggestions(ConversationContext context, MarkOccurrenceProposal proposal) {
        List<String> suggestedMarkIds = proposalFlowService.getSuggestedMarkIds(proposal.getId());

        if (suggestedMarkIds.isEmpty()) {
            log.info("No suggested mark IDs found for proposal ID: {}", proposal.getId());
            return handleNoMarksFound(context);
        }

        log.info("Found {} suggested marks for proposal ID: {}", suggestedMarkIds.size(), proposal.getId());

        if (suggestedMarkIds.size() == 1) {
            return processSingleMatch(context, proposal, suggestedMarkIds.getFirst());
        } else {
            return processMultipleMatches(context, proposal, suggestedMarkIds);
        }
    }

    @Override
    public List<BotResponse> processSingleMatch(ConversationContext context, MarkOccurrenceProposal proposal, String markId) {
        context.setCurrentState(ConversationState.WAITING_FOR_MARK_CONFIRMATION);
        
        Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));
        if (markOptional.isPresent()) {
            Mark mark = markOptional.get();
            Long mediaId = (mark.getCover() != null) ? mark.getCover().getId() : null;
            
            List<BotResponse> responses = new ArrayList<>();

            // 1. First response: The Photo (if available)
            if (mediaId != null) {
                 // Send a text message first as title
                 responses.add(BotResponse.builder()
                         .uiComponent(TextMessage.builder().text("I found a mark that looks similar:").build())
                         .build());

                 // Send the photo item
                 PhotoItem photoItem = PhotoItem.builder()
                            .mediaFileId(mediaId)
                            .caption("Mark #" + mark.getId())
                            .callbackData("noop") 
                            .build();
                 responses.add(BotResponse.builder().uiComponent(photoItem).build());
            }

            // 2. Second response: The Question and Buttons
            Menu confirmationMenu = Menu.builder()
                    .title("Does it match with the one that you uploaded?")
                    .buttons(List.of(
                            List.of(
                                    Button.builder().text("✅ Yes").callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + mark.getId()).build(),
                                    Button.builder().text("❌ No").callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                            )
                    ))
                    .build();
            responses.add(BotResponse.builder().uiComponent(confirmationMenu).build());

            return responses;
        }
        
        return handleNoMarksFound(context);
    }

    @Override
    public List<BotResponse> processMultipleMatches(ConversationContext context, MarkOccurrenceProposal proposal, List<String> markIds) {
        context.setCurrentState(ConversationState.AWAITING_MARK_SELECTION);
        List<BotResponse> responses = new ArrayList<>();

        responses.add(BotResponse.builder()
                .uiComponent(TextMessage.builder().text("I found some marks that might match. Please select one or propose a new one:").build())
                .build());

        for (String markId : markIds) {
            Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));
            markOptional.ifPresent(mark -> {
                Long mediaId = (mark.getCover() != null) ? mark.getCover().getId() : null;
                if (mediaId == null) {
                    log.warn("Mark {} has no cover loaded", mark.getId());
                }

                String caption = "Mark " + mark.getId();

                PhotoItem photoItem = PhotoItem.builder()
                        .mediaFileId(mediaId)
                        .caption(caption)
                        .callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId())
                        .build();
                responses.add(BotResponse.builder().uiComponent(photoItem).build());
            });
        }

        Menu proposeNewMenu = Menu.builder()
                .title("If none of above options match")
                .buttons(List.of(
                        List.of(Button.builder().text("Propose New Mark").callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build())
                ))
                .build();
        responses.add(BotResponse.builder().uiComponent(proposeNewMenu).build());

        return responses;
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
