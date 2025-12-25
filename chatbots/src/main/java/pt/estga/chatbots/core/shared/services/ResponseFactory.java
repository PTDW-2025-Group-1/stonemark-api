package pt.estga.chatbots.core.shared.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbots.core.proposal.ProposalCallbackData;
import pt.estga.chatbots.core.shared.Messages;
import pt.estga.chatbots.core.shared.SharedCallbackData;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.HandlerOutcome;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.models.ui.PhotoItem;
import pt.estga.chatbots.core.shared.models.ui.TextMessage;
import pt.estga.chatbots.core.verification.VerificationCallbackData;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Map.entry;

@Service
@RequiredArgsConstructor
public class ResponseFactory {

    private final UiTextService textService;
    private final MarkService markService;
    private final MonumentService monumentService;

    public List<BotResponse> createResponse(ConversationContext context, HandlerOutcome outcome) {
        ConversationState currentState = context.getCurrentState();

        if (outcome == HandlerOutcome.FAILURE) {
            return createErrorResponse(context);
        }

        // Special case for successful verification, which has a dynamic message
        if (currentState == ConversationState.AWAITING_VERIFICATION_CODE && outcome == HandlerOutcome.SUCCESS) {
            return buildSimpleMenuResponse(Messages.VERIFICATION_SUCCESS_CODE);
        }
        
        // Special case for successful submission
        if (currentState == ConversationState.SUBMITTED && outcome == HandlerOutcome.SUCCESS) {
            return buildSimpleMenuResponse(Messages.SUBMISSION_SUCCESS);
        }

        return switch (currentState) {
            case AWAITING_PROPOSAL_ACTION -> createProposalActionResponse();
            case LOOP_OPTIONS -> createLoopOptionsResponse();
            case WAITING_FOR_MARK_CONFIRMATION -> createSingleMarkConfirmationResponse(context);
            case AWAITING_MARK_SELECTION -> createMultipleMarkSelectionResponse(context);
            case AWAITING_NEW_MARK_DETAILS -> createNewMarkDetailsResponse();
            case WAITING_FOR_MONUMENT_CONFIRMATION -> createMonumentConfirmationResponse(context);
            case AWAITING_NEW_MONUMENT_NAME -> buildSimpleMenuResponse(Messages.PROVIDE_NEW_MONUMENT_NAME_PROMPT);
            case SUBMISSION_LOOP_OPTIONS -> createSubmissionLoopResponse();
            case AWAITING_DISCARD_CONFIRMATION -> createDiscardConfirmationResponse();
            case AWAITING_NOTES -> createNotesResponse();
            case AWAITING_VERIFICATION_METHOD -> createVerificationMethodResponse();
            case AWAITING_CONTACT -> buildSimpleMenuResponse(Messages.SHARE_CONTACT_PROMPT);
            case AWAITING_VERIFICATION_CODE -> buildSimpleMenuResponse(Messages.ENTER_VERIFICATION_CODE_PROMPT);
            default -> {
                String messageKey = getEntryMessageForState(currentState);
                yield buildSimpleMenuResponse(messageKey);
            }
        };
    }
    
    public List<BotResponse> createErrorResponse(ConversationContext context) {
        String messageKey = getFailureMessageForState(context.getCurrentState());
        return buildSimpleMenuResponse(messageKey);
    }

    private List<BotResponse> createProposalActionResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.INCOMPLETE_SUBMISSION_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_SUBMISSION_BTN)).callbackData(ProposalCallbackData.CONTINUE_PROPOSAL).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.START_NEW_SUBMISSION_BTN)).callbackData(ProposalCallbackData.DELETE_AND_START_NEW).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createLoopOptionsResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.LOOP_OPTIONS_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.CHANGE_LOCATION_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CHANGE_PHOTO_BTN)).callbackData(ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_BTN)).callbackData(ProposalCallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createSingleMarkConfirmationResponse(ConversationContext context) {
        String markId = context.getSuggestedMarkIds().getFirst();
        Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));

        if (markOptional.isEmpty()) {
            return createErrorResponse(context);
        }

        Mark mark = markOptional.get();
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(Messages.FOUND_SINGLE_MARK_TITLE)).build()).build());

        if (mark.getCover() != null) {
            PhotoItem photoItem = PhotoItem.builder().mediaFileId(mark.getCover().getId()).captionNode(textService.get("Mark #" + mark.getId())).build();
            responses.add(BotResponse.builder().uiComponent(photoItem).build());
        }

        Menu confirmationMenu = Menu.builder()
                .titleNode(textService.get(Messages.MATCH_CONFIRMATION_TITLE))
                .buttons(List.of(List.of(
                        Button.builder().textNode(textService.get(Messages.YES_BTN)).callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + mark.getId()).build(),
                        Button.builder().textNode(textService.get(Messages.NO_BTN)).callbackData(ProposalCallbackData.CONFIRM_MARK_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                ))).build();
        responses.add(BotResponse.builder().uiComponent(confirmationMenu).build());

        return responses;
    }

    private List<BotResponse> createMultipleMarkSelectionResponse(ConversationContext context) {
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(Messages.FOUND_MARKS_TITLE)).build()).build());

        for (String markId : context.getSuggestedMarkIds()) {
            markService.findWithCoverById(Long.valueOf(markId)).ifPresent(mark -> {
                PhotoItem photoItem = PhotoItem.builder()
                        .mediaFileId(mark.getCover() != null ? mark.getCover().getId() : null)
                        .captionNode(textService.get("Mark " + mark.getId()))
                        .callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId())
                        .build();
                responses.add(BotResponse.builder().uiComponent(photoItem).build());
            });
        }

        Menu proposeNewMenu = Menu.builder()
                .titleNode(textService.get("If none of above options match"))
                .buttons(List.of(List.of(Button.builder().textNode(textService.get(Messages.PROPOSE_NEW_MARK_BTN)).callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build())))
                .build();
        responses.add(BotResponse.builder().uiComponent(proposeNewMenu).build());

        return responses;
    }

    private List<BotResponse> createNewMarkDetailsResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.PROVIDE_NEW_MARK_DETAILS_PROMPT))
                .buttons(List.of(List.of(Button.builder().textNode(textService.get(Messages.SKIP_MARK_DETAILS_BTN)).callbackData(ProposalCallbackData.SKIP_MARK_DETAILS).build())))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createMonumentConfirmationResponse(ConversationContext context) {
        if (context.getSuggestedMonumentIds() == null || context.getSuggestedMonumentIds().isEmpty()) {
            return createErrorResponse(context);
        }
        String monumentId = context.getSuggestedMonumentIds().getFirst();
        Optional<Monument> monumentOptional = monumentService.findById(Long.valueOf(monumentId));

        if (monumentOptional.isEmpty()) {
            return createErrorResponse(context);
        }

        Monument monument = monumentOptional.get();
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.MONUMENT_CONFIRMATION_TITLE, monument.getName()))
                .buttons(List.of(List.of(
                        Button.builder().textNode(textService.get(Messages.YES_BTN)).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_YES + ":" + monument.getId()).build(),
                        Button.builder().textNode(textService.get(Messages.NO_BTN)).callbackData(ProposalCallbackData.CONFIRM_MONUMENT_PREFIX + SharedCallbackData.CONFIRM_NO).build()
                ))).build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createSubmissionLoopResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.SUBMISSION_LOOP_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.DISCARD_SUBMISSION_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.CONTINUE_TO_SUBMIT_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createDiscardConfirmationResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.DISCARD_CONFIRMATION_TITLE))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.YES_DISCARD_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.NO_GO_BACK_BTN)).callbackData(ProposalCallbackData.SUBMISSION_LOOP_OPTIONS).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }
    
    private List<BotResponse> createNotesResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.ADD_NOTES_PROMPT))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.SKIP_BTN)).callbackData(ProposalCallbackData.SKIP_NOTES).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }
    
    private List<BotResponse> createVerificationMethodResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(Messages.CHOOSE_VERIFICATION_METHOD_PROMPT))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(Messages.VERIFY_WITH_CODE_BTN)).callbackData(VerificationCallbackData.CHOOSE_VERIFY_WITH_CODE).build()),
                        List.of(Button.builder().textNode(textService.get(Messages.VERIFY_WITH_PHONE_BTN)).callbackData(VerificationCallbackData.CHOOSE_VERIFY_WITH_PHONE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> buildSimpleMenuResponse(String messageKey) {
        if (messageKey == null) {
            return Collections.singletonList(BotResponse.builder().textNode(textService.get(Messages.ERROR_GENERIC)).build());
        }
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().titleNode(textService.get(messageKey)).build())
                .build());
    }

    private String getEntryMessageForState(ConversationState state) {
        return Map.of(
                ConversationState.WAITING_FOR_PHOTO, Messages.REQUEST_PHOTO_PROMPT,
                ConversationState.AWAITING_LOCATION, Messages.REQUEST_LOCATION_PROMPT
        ).get(state);
    }

    private String getFailureMessageForState(ConversationState state) {
        return Map.ofEntries(
                entry(ConversationState.WAITING_FOR_PHOTO, Messages.EXPECTING_PHOTO_ERROR),
                entry(ConversationState.AWAITING_LOCATION, Messages.EXPECTING_LOCATION_ERROR),
                entry(ConversationState.LOOP_OPTIONS, Messages.INVALID_SELECTION),
                entry(ConversationState.AWAITING_PROPOSAL_ACTION, Messages.INVALID_SELECTION),
                entry(ConversationState.AWAITING_PHOTO_ANALYSIS, Messages.ERROR_PROCESSING_PHOTO),
                entry(ConversationState.AWAITING_MONUMENT_SUGGESTIONS, Messages.ERROR_GENERIC),
                entry(ConversationState.SUBMISSION_LOOP_OPTIONS, Messages.INVALID_SELECTION),
                entry(ConversationState.AWAITING_DISCARD_CONFIRMATION, Messages.INVALID_SELECTION),
                entry(ConversationState.AWAITING_VERIFICATION_CODE, Messages.INVALID_CODE_ERROR),
                entry(ConversationState.AWAITING_CONTACT, Messages.USER_NOT_FOUND_ERROR),
                entry(ConversationState.AWAITING_VERIFICATION_METHOD, Messages.INVALID_SELECTION)
        ).get(state);
    }
}
