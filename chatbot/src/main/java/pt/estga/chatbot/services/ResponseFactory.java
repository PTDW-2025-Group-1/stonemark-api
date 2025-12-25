package pt.estga.chatbot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.Messages;
import pt.estga.chatbot.SharedCallbackData;
import pt.estga.chatbot.context.ConversationContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.context.VerificationState;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.ui.Button;
import pt.estga.chatbot.models.ui.Menu;
import pt.estga.chatbot.models.ui.PhotoItem;
import pt.estga.chatbot.models.ui.TextMessage;
import pt.estga.chatbot.features.verification.VerificationCallbackData;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ResponseFactory {

    private final UiTextService textService;
    private final MarkService markService;
    private final MonumentService monumentService;
    private final MainMenuFactory mainMenuFactory;

    public List<BotResponse> createResponse(ConversationContext context, HandlerOutcome outcome, BotInput input) {
        ConversationState currentState = context.getCurrentState();

        if (outcome == HandlerOutcome.FAILURE) {
            return createErrorResponse(context);
        }

        // Special case for successful verification
        if (currentState == VerificationState.AWAITING_VERIFICATION_CODE && outcome == HandlerOutcome.SUCCESS) {
            return buildSimpleMenuResponse(Messages.VERIFICATION_SUCCESS_CODE);
        }
        
        // Special case for successful submission
        if (currentState == ProposalState.SUBMITTED && outcome == HandlerOutcome.SUCCESS) {
            List<BotResponse> responses = new ArrayList<>();
            responses.add(buildSimpleMenuResponse(Messages.SUBMISSION_SUCCESS).getFirst());
            responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
            return responses;
        }

        if (currentState instanceof ProposalState proposalState) {
            return handleProposalState(proposalState, context);
        } else if (currentState instanceof VerificationState verificationState) {
            return handleVerificationState(verificationState);
        } else if (currentState instanceof CoreState coreState) {
            return handleCoreState(coreState);
        }

        return createErrorResponse(context);
    }
    
    private List<BotResponse> handleProposalState(ProposalState state, ConversationContext context) {
        return switch (state) {
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
            default -> {
                String messageKey = getEntryMessageForState(state);
                yield buildSimpleMenuResponse(messageKey);
            }
        };
    }

    private List<BotResponse> handleVerificationState(VerificationState state) {
        return switch (state) {
            case AWAITING_VERIFICATION_METHOD -> createVerificationMethodResponse();
            case AWAITING_CONTACT -> buildSimpleMenuResponse(Messages.SHARE_CONTACT_PROMPT);
            case AWAITING_VERIFICATION_CODE -> buildSimpleMenuResponse(Messages.ENTER_VERIFICATION_CODE_PROMPT);
            default -> {
                String messageKey = getEntryMessageForState(state);
                yield buildSimpleMenuResponse(messageKey);
            }
        };
    }

    private List<BotResponse> handleCoreState(CoreState state) {
        String messageKey = getEntryMessageForState(state);
        return buildSimpleMenuResponse(messageKey);
    }
    
    public List<BotResponse> createErrorResponse(ConversationContext context) {
        String messageKey = getFailureMessageForState(context.getCurrentState());
        return buildSimpleMenuResponse(messageKey);
    }

    // ... (Helper methods for creating specific responses remain the same) ...

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
        if (state instanceof ProposalState proposalState) {
            return switch (proposalState) {
                case WAITING_FOR_PHOTO -> Messages.REQUEST_PHOTO_PROMPT;
                case AWAITING_LOCATION -> Messages.REQUEST_LOCATION_PROMPT;
                default -> null;
            };
        }
        return null;
    }

    private String getFailureMessageForState(ConversationState state) {
        if (state instanceof ProposalState proposalState) {
            return switch (proposalState) {
                case WAITING_FOR_PHOTO -> Messages.EXPECTING_PHOTO_ERROR;
                case AWAITING_LOCATION -> Messages.EXPECTING_LOCATION_ERROR;
                case LOOP_OPTIONS -> Messages.INVALID_SELECTION;
                case AWAITING_PROPOSAL_ACTION -> Messages.INVALID_SELECTION;
                case AWAITING_PHOTO_ANALYSIS -> Messages.ERROR_PROCESSING_PHOTO;
                case AWAITING_MONUMENT_SUGGESTIONS -> Messages.ERROR_GENERIC;
                case SUBMISSION_LOOP_OPTIONS -> Messages.INVALID_SELECTION;
                case AWAITING_DISCARD_CONFIRMATION -> Messages.INVALID_SELECTION;
                default -> null;
            };
        } else if (state instanceof VerificationState verificationState) {
            return switch (verificationState) {
                case AWAITING_VERIFICATION_CODE -> Messages.INVALID_CODE_ERROR;
                case AWAITING_CONTACT -> Messages.USER_NOT_FOUND_ERROR;
                case AWAITING_VERIFICATION_METHOD -> Messages.INVALID_SELECTION;
                default -> null;
            };
        }
        return null;
    }
}
