package pt.estga.chatbot.features.proposal;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import pt.estga.chatbot.constants.MessageKey;
import pt.estga.chatbot.constants.SharedCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.Message;
import pt.estga.chatbot.models.ui.*;
import pt.estga.chatbot.features.core.MainMenuFactory;
import pt.estga.chatbot.services.ResponseProvider;
import pt.estga.chatbot.services.UiTextService;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkQueryService;
import pt.estga.content.services.MonumentQueryService;
import pt.estga.proposal.entities.MarkOccurrenceProposal;
import pt.estga.proposal.entities.Proposal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pt.estga.chatbot.constants.EmojiKey.*;

@Component
@RequiredArgsConstructor
public class ProposalResponseProvider implements ResponseProvider {

    private final UiTextService textService;
    private final MarkQueryService markQueryService;
    private final MonumentQueryService monumentQueryService;
    private final MainMenuFactory mainMenuFactory;

    @Value("${application.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public boolean supports(ConversationState state) {
        return state instanceof ProposalState;
    }

    @Override
    public List<BotResponse> createResponse(ChatbotContext context, HandlerOutcome outcome, BotInput input) {
        ProposalState state = (ProposalState) context.getCurrentState();
        return switch (state) {
            case PROPOSAL_START -> Collections.emptyList();
            case AWAITING_LOCATION -> createLocationRequestResponse();
            case WAITING_FOR_MARK_CONFIRMATION -> createSingleMarkConfirmationResponse(context);
            case AWAITING_MARK_SELECTION -> createMultipleMarkSelectionResponse(context);
            case MARK_SELECTED -> createMarkSelectedResponse(context);
            case AWAITING_MONUMENT_SUGGESTIONS, AWAITING_MONUMENT_SELECTION -> createMonumentSuggestionsResponse(context);
            case WAITING_FOR_MONUMENT_CONFIRMATION -> createMonumentConfirmationResponse(context);
            case AWAITING_NOTES -> createNotesResponse();
            case SUBMITTED -> createSubmissionSuccessResponse(input);
            default -> {
                Message message = getEntryMessageForState(state);
                yield buildSimpleMenuResponse(message);
            }
        };
    }

    private List<BotResponse> createSingleMarkConfirmationResponse(ChatbotContext context) {
        String markId = context.getProposalContext().getSuggestedMarkIds().getFirst();
        Optional<Mark> markOptional = markQueryService.findWithCoverById(Long.valueOf(markId));

        if (markOptional.isEmpty()) {
            return buildSimpleMenuResponse(new Message(MessageKey.ERROR_GENERIC, WARNING));
        }

        Mark mark = markOptional.get();
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(new Message(MessageKey.FOUND_SINGLE_MARK_TITLE))).build()).build());

        if (mark.getCover() != null) {
            PhotoItem photoItem = PhotoItem.builder()
                    .mediaFileId(mark.getCover().getId())
                    .captionNode(textService.get(new Message(MessageKey.MARK_CAPTION, mark.getId())))
                    .build();
            responses.add(BotResponse.builder().uiComponent(photoItem).build());
        }

        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(new Message(MessageKey.MARK_DESCRIPTION, frontendBaseUrl, mark.getId()))).build()).build());

        Menu confirmationMenu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.MATCH_CONFIRMATION_TITLE)))
                .buttons(List.of(List.of(
                        Button.builder().textNode(textService.get(new Message(MessageKey.YES_BTN, CHECK))).callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + mark.getId()).build(),
                        Button.builder().textNode(textService.get(new Message(MessageKey.NO_BTN, CROSS))).callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                ))).build();
        responses.add(BotResponse.builder().uiComponent(confirmationMenu).build());

        return responses;
    }

    private List<BotResponse> createMultipleMarkSelectionResponse(ChatbotContext context) {
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(new Message(MessageKey.FOUND_MARKS_TITLE, SEARCH))).build()).build());

        for (String markId : context.getProposalContext().getSuggestedMarkIds()) {
            markQueryService.findWithCoverById(Long.valueOf(markId)).ifPresent(mark -> {
                PhotoItem.PhotoItemBuilder photoItemBuilder = PhotoItem.builder()
                        .captionNode(textService.get(new Message(MessageKey.MARK_CAPTION, mark.getId())));

                if (mark.getCover() != null) {
                    photoItemBuilder.mediaFileId(mark.getCover().getId());
                }

                responses.add(BotResponse.builder().uiComponent(photoItemBuilder.build()).build());

                Menu selectionMenu = Menu.builder()
                        .titleNode(textService.get(new Message(MessageKey.MARK_DESCRIPTION, frontendBaseUrl, mark.getId())))
                        .buttons(List.of(List.of(
                                Button.builder().textNode(textService.get(new Message(MessageKey.SELECT_BTN, CHECK))).callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId()).build()
                        ))).build();

                responses.add(BotResponse.builder().uiComponent(selectionMenu).build());
            });
        }

        Menu proposeNewMenu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.IF_NONE_OF_ABOVE_OPTIONS_MATCH)))
                .buttons(List.of(List.of(Button.builder().textNode(textService.get(new Message(MessageKey.PROPOSE_NEW_MARK_BTN, NEW))).callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build())))
                .build();
        responses.add(BotResponse.builder().uiComponent(proposeNewMenu).build());

        return responses;
    }

    private List<BotResponse> createMarkSelectedResponse(ChatbotContext context) {
        Proposal proposal = context.getProposalContext().getProposal();
        if (proposal instanceof MarkOccurrenceProposal markProposal) {
            if (markProposal.getExistingMark() != null) {
                Long markId = markProposal.getExistingMark().getId();
                return buildSimpleMenuResponse(new Message(MessageKey.MARK_SELECTED_CONFIRMATION, markId));
            }
        }
        return buildSimpleMenuResponse(new Message(MessageKey.ERROR_GENERIC, WARNING));
    }

    private List<BotResponse> createMonumentSuggestionsResponse(ChatbotContext context) {
        List<String> suggestedMonumentIds = context.getProposalContext().getSuggestedMonumentIds();

        if (suggestedMonumentIds == null || suggestedMonumentIds.isEmpty()) {
            return buildSimpleMenuResponse(new Message(MessageKey.NO_MONUMENTS_FOUND));
        }

        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(new Message(MessageKey.FOUND_MONUMENTS_TITLE, SEARCH))).build()).build());

        for (String monumentId : suggestedMonumentIds) {
            monumentQueryService.findById(Long.valueOf(monumentId)).ifPresent(monument -> {
                Menu selectionMenu = Menu.builder()
                        .titleNode(textService.get(new Message(MessageKey.MONUMENT_OPTION, monument.getName())))
                        .buttons(List.of(List.of(
                                Button.builder().textNode(textService.get(new Message(MessageKey.SELECT_BTN, CHECK))).callbackData(ProposalCallbackData.SELECT_MONUMENT_PREFIX + monument.getId()).build()
                        ))).build();
                responses.add(BotResponse.builder().uiComponent(selectionMenu).build());
            });
        }

        return responses;
    }

    private List<BotResponse> createMonumentConfirmationResponse(ChatbotContext context) {
        if (context.getProposalContext().getSuggestedMonumentIds() == null || context.getProposalContext().getSuggestedMonumentIds().isEmpty()) {
            return buildSimpleMenuResponse(new Message(MessageKey.ERROR_GENERIC, WARNING));
        }
        String monumentId = context.getProposalContext().getSuggestedMonumentIds().getFirst();
        Optional<Monument> monumentOptional = monumentQueryService.findById(Long.valueOf(monumentId));

        if (monumentOptional.isEmpty()) {
            return buildSimpleMenuResponse(new Message(MessageKey.ERROR_GENERIC, WARNING));
        }

        Monument monument = monumentOptional.get();
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.MONUMENT_CONFIRMATION_TITLE, monument.getName())))
                .buttons(List.of(List.of(
                        Button.builder().textNode(textService.get(new Message(MessageKey.YES_BTN, CHECK))).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + monument.getId()).build(),
                        Button.builder().textNode(textService.get(new Message(MessageKey.NO_BTN, CROSS))).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                ))).build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createNotesResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.ADD_NOTES_PROMPT, MEMO)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.SKIP_BTN, ARROW_RIGHT))).callbackData(ProposalCallbackData.SKIP_NOTES).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createLocationRequestResponse() {
        LocationRequest locationRequest = LocationRequest.builder()
                .messageNode(textService.get(new Message(MessageKey.REQUEST_LOCATION_PROMPT, LOCATION, PAPERCLIP)))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(locationRequest).build());
    }

    private List<BotResponse> createSubmissionSuccessResponse(BotInput input) {
        List<BotResponse> responses = new ArrayList<>();
        responses.add(buildSimpleMenuResponse(new Message(MessageKey.SUBMISSION_SUCCESS, TADA)).getFirst());
        responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
        return responses;
    }

    private List<BotResponse> buildSimpleMenuResponse(Message message) {
        if (message == null) {
            return Collections.singletonList(BotResponse.builder().textNode(textService.get(new Message(MessageKey.ERROR_GENERIC, WARNING))).build());
        }
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().titleNode(textService.get(message)).build())
                .build());
    }

    private Message getEntryMessageForState(ProposalState state) {
        return switch (state) {
            case WAITING_FOR_PHOTO -> new Message(MessageKey.REQUEST_PHOTO_PROMPT, CAMERA);
            case AWAITING_LOCATION -> new Message(MessageKey.REQUEST_LOCATION_PROMPT, LOCATION, PAPERCLIP);
            default -> null;
        };
    }
}
