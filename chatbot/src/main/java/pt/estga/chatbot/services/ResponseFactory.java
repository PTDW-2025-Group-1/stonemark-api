package pt.estga.chatbot.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pt.estga.chatbot.constants.MessageKey;
import pt.estga.chatbot.constants.SharedCallbackData;
import pt.estga.chatbot.context.ChatbotContext;
import pt.estga.chatbot.context.ConversationState;
import pt.estga.chatbot.context.CoreState;
import pt.estga.chatbot.context.HandlerOutcome;
import pt.estga.chatbot.context.ProposalState;
import pt.estga.chatbot.context.VerificationState;
import pt.estga.chatbot.features.proposal.ProposalCallbackData;
import pt.estga.chatbot.features.verification.VerificationCallbackData;
import pt.estga.chatbot.models.BotInput;
import pt.estga.chatbot.models.BotResponse;
import pt.estga.chatbot.models.Message;
import pt.estga.chatbot.models.ui.*;
import pt.estga.content.entities.Mark;
import pt.estga.content.entities.Monument;
import pt.estga.content.services.MarkService;
import pt.estga.content.services.MonumentService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static pt.estga.chatbot.constants.EmojiKey.*;

@Service
@RequiredArgsConstructor
public class ResponseFactory {

    private final UiTextService textService;
    private final MarkService markService;
    private final MonumentService monumentService;
    private final MainMenuFactory mainMenuFactory;

    public List<BotResponse> createResponse(ChatbotContext context, HandlerOutcome outcome, BotInput input) {
        ConversationState currentState = context.getCurrentState();

        if (outcome == HandlerOutcome.FAILURE) {
            return createErrorResponse(context);
        }

        if (currentState instanceof ProposalState proposalState) {
            return handleProposalState(proposalState, context, input);
        } else if (currentState instanceof VerificationState verificationState) {
            return handleVerificationState(verificationState, context, input);
        } else if (currentState instanceof CoreState coreState) {
            return handleCoreState(coreState, context, input);
        }

        return createErrorResponse(context);
    }

    private List<BotResponse> handleProposalState(ProposalState state, ChatbotContext context, BotInput input) {
        return switch (state) {
            case AWAITING_LOCATION -> createLocationRequestResponse();
            case AWAITING_PROPOSAL_ACTION -> createProposalActionResponse();
            case LOOP_OPTIONS -> createLoopOptionsResponse();
            case WAITING_FOR_MARK_CONFIRMATION -> createSingleMarkConfirmationResponse(context);
            case AWAITING_MARK_SELECTION -> createMultipleMarkSelectionResponse(context);
            case AWAITING_NEW_MARK_DETAILS -> createNewMarkDetailsResponse();
            case WAITING_FOR_MONUMENT_CONFIRMATION -> createMonumentConfirmationResponse(context);
            case AWAITING_NEW_MONUMENT_NAME ->
                    buildSimpleMenuResponse(new Message(MessageKey.PROVIDE_NEW_MONUMENT_NAME_PROMPT, MONUMENT));
            case SUBMISSION_LOOP_OPTIONS -> createSubmissionLoopResponse();
            case AWAITING_DISCARD_CONFIRMATION -> createDiscardConfirmationResponse();
            case AWAITING_NOTES -> createNotesResponse();
            case SUBMITTED -> createSubmissionSuccessResponse(input);
            default -> {
                Message message = getEntryMessageForState(state);
                yield buildSimpleMenuResponse(message);
            }
        };
    }

    private List<BotResponse> handleVerificationState(VerificationState state, ChatbotContext context, BotInput input) {
        return switch (state) {
            case AWAITING_VERIFICATION_METHOD -> createVerificationMethodResponse();
            case AWAITING_CONTACT -> createContactRequestResponse();
            case AWAITING_VERIFICATION_CODE ->
                    buildSimpleMenuResponse(new Message(MessageKey.ENTER_VERIFICATION_CODE_PROMPT, NUMBERS));
            case AWAITING_PHONE_CONNECTION_DECISION -> {
                List<BotResponse> responses = new ArrayList<>();
                responses.add(buildSimpleMenuResponse(new Message(MessageKey.VERIFICATION_SUCCESS_CODE, context.getUserName(), TADA)).getFirst());
                responses.add(createPhoneConnectionPrompt().getFirst());
                yield responses;
            }
            case PHONE_VERIFICATION_SUCCESS -> {
                List<BotResponse> responses = new ArrayList<>();
                responses.add(buildSimpleMenuResponse(new Message(MessageKey.VERIFICATION_SUCCESS_PHONE, TADA)).getFirst());
                responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
                yield responses;
            }
            case PHONE_CONNECTION_SUCCESS -> {
                List<BotResponse> responses = new ArrayList<>();
                responses.add(buildSimpleMenuResponse(new Message(MessageKey.PHONE_CONNECTION_SUCCESS, TADA)).getFirst());
                responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
                yield responses;
            }
        };
    }

    private List<BotResponse> handleCoreState(CoreState state, ChatbotContext context, BotInput input) {
        return switch (state) {
            case MAIN_MENU -> {
                List<BotResponse> responses = new ArrayList<>();
                Message welcomeMessage;
                if (context.getUserName() != null) {
                    welcomeMessage = new Message(MessageKey.WELCOME_BACK, context.getUserName(), WAVE);
                } else {
                    welcomeMessage = new Message(MessageKey.WELCOME, WAVE);
                }
                responses.add(BotResponse.builder().textNode(textService.get(welcomeMessage)).build());
                responses.add(BotResponse.builder().uiComponent(mainMenuFactory.create(input)).build());
                yield responses;
            }
            default -> {
                Message message = getEntryMessageForState(state);
                yield buildSimpleMenuResponse(message);
            }
        };
    }

    public List<BotResponse> createErrorResponse(ChatbotContext context) {
        Message message = getFailureMessageForState(context.getCurrentState());
        return buildSimpleMenuResponse(message);
    }

    private List<BotResponse> createProposalActionResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.INCOMPLETE_SUBMISSION_TITLE)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.CONTINUE_SUBMISSION_BTN, ARROW_RIGHT))).callbackData(ProposalCallbackData.CONTINUE_PROPOSAL).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.START_NEW_SUBMISSION_BTN, TRASH))).callbackData(ProposalCallbackData.DELETE_AND_START_NEW).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createLoopOptionsResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.LOOP_OPTIONS_TITLE)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.CHANGE_LOCATION_BTN, LOCATION))).callbackData(ProposalCallbackData.LOOP_REDO_LOCATION).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.CHANGE_PHOTO_BTN, CAMERA))).callbackData(ProposalCallbackData.LOOP_REDO_IMAGE_UPLOAD).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.CONTINUE_BTN, ARROW_RIGHT))).callbackData(ProposalCallbackData.LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createSingleMarkConfirmationResponse(ChatbotContext context) {
        String markId = context.getProposalContext().getSuggestedMarkIds().getFirst();
        Optional<Mark> markOptional = markService.findWithCoverById(Long.valueOf(markId));

        if (markOptional.isEmpty()) {
            return createErrorResponse(context);
        }

        Mark mark = markOptional.get();
        List<BotResponse> responses = new ArrayList<>();
        responses.add(BotResponse.builder().uiComponent(TextMessage.builder().textNode(textService.get(new Message(MessageKey.FOUND_SINGLE_MARK_TITLE))).build()).build());

        if (mark.getCover() != null) {
            PhotoItem photoItem = PhotoItem.builder().mediaFileId(mark.getCover().getId()).captionNode(textService.get(new Message(MessageKey.MARK_CAPTION, mark.getId()))).build();
            responses.add(BotResponse.builder().uiComponent(photoItem).build());
        }

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
            markService.findWithCoverById(Long.valueOf(markId)).ifPresent(mark -> {
                PhotoItem photoItem = PhotoItem.builder()
                        .mediaFileId(mark.getCover() != null ? mark.getCover().getId() : null)
                        .captionNode(textService.get(new Message(MessageKey.MARK_CAPTION, mark.getId())))
                        .callbackData(ProposalCallbackData.SELECT_MARK_PREFIX + mark.getId())
                        .build();
                responses.add(BotResponse.builder().uiComponent(photoItem).build());
            });
        }

        Menu proposeNewMenu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.IF_NONE_OF_ABOVE_OPTIONS_MATCH)))
                .buttons(List.of(List.of(Button.builder().textNode(textService.get(new Message(MessageKey.PROPOSE_NEW_MARK_BTN, NEW))).callbackData(ProposalCallbackData.PROPOSE_NEW_MARK).build())))
                .build();
        responses.add(BotResponse.builder().uiComponent(proposeNewMenu).build());

        return responses;
    }

    private List<BotResponse> createNewMarkDetailsResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.PROVIDE_NEW_MARK_DETAILS_PROMPT, MEMO)))
                .buttons(List.of(List.of(Button.builder().textNode(textService.get(new Message(MessageKey.SKIP_BTN, ARROW_RIGHT))).callbackData(ProposalCallbackData.SKIP_MARK_DETAILS).build())))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createMonumentConfirmationResponse(ChatbotContext context) {
        if (context.getProposalContext().getSuggestedMonumentIds() == null || context.getProposalContext().getSuggestedMonumentIds().isEmpty()) {
            return createErrorResponse(context);
        }
        String monumentId = context.getProposalContext().getSuggestedMonumentIds().getFirst();
        Optional<Monument> monumentOptional = monumentService.findById(Long.valueOf(monumentId));

        if (monumentOptional.isEmpty()) {
            return createErrorResponse(context);
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

    private List<BotResponse> createSubmissionLoopResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.SUBMISSION_LOOP_TITLE)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.DISCARD_SUBMISSION_BTN, TRASH))).callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.CONTINUE_TO_SUBMIT_BTN, ARROW_RIGHT))).callbackData(ProposalCallbackData.SUBMISSION_LOOP_CONTINUE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createDiscardConfirmationResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.DISCARD_CONFIRMATION_TITLE, WARNING)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.YES_DISCARD_BTN, TRASH)))
                                .callbackData(ProposalCallbackData.SUBMISSION_LOOP_START_OVER_CONFIRMED).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.NO_GO_BACK_BTN, BACK)))
                                .callbackData(ProposalCallbackData.SUBMISSION_LOOP_OPTIONS).build())
                ))
                .build();
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

    private List<BotResponse> createVerificationMethodResponse() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.CHOOSE_VERIFICATION_METHOD_PROMPT)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.VERIFY_WITH_CODE_BTN, NUMBERS))).callbackData(VerificationCallbackData.CHOOSE_VERIFY_WITH_CODE).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.VERIFY_WITH_PHONE_BTN, PHONE))).callbackData(VerificationCallbackData.CHOOSE_VERIFY_WITH_PHONE).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
    }

    private List<BotResponse> createContactRequestResponse() {
        ContactRequest contactRequest = ContactRequest.builder()
                .messageNode(textService.get(new Message(MessageKey.SHARE_PHONE_NUMBER_PROMPT, PHONE)))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(contactRequest).build());
    }

    private List<BotResponse> createLocationRequestResponse() {
        LocationRequest locationRequest = LocationRequest.builder()
                .messageNode(textService.get(new Message(MessageKey.REQUEST_LOCATION_PROMPT, LOCATION, PAPERCLIP)))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(locationRequest).build());
    }

    private List<BotResponse> createPhoneConnectionPrompt() {
        Menu menu = Menu.builder()
                .titleNode(textService.get(new Message(MessageKey.PROMPT_CONNECT_PHONE)))
                .buttons(List.of(
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.YES_BTN, CHECK))).callbackData(VerificationCallbackData.CONNECT_PHONE_YES).build()),
                        List.of(Button.builder().textNode(textService.get(new Message(MessageKey.NO_BTN, CROSS))).callbackData(VerificationCallbackData.CONNECT_PHONE_NO).build())
                ))
                .build();
        return Collections.singletonList(BotResponse.builder().uiComponent(menu).build());
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

    private Message getEntryMessageForState(ConversationState state) {
        if (state instanceof ProposalState proposalState) {
            return switch (proposalState) {
                case WAITING_FOR_PHOTO -> new Message(MessageKey.REQUEST_PHOTO_PROMPT, CAMERA);
                case AWAITING_LOCATION -> new Message(MessageKey.REQUEST_LOCATION_PROMPT, LOCATION, PAPERCLIP);
                default -> null;
            };
        } else if (state instanceof VerificationState) {
            return null;
        }
        return null;
    }

    private Message getFailureMessageForState(ConversationState state) {
        if (state instanceof ProposalState proposalState) {
            return switch (proposalState) {
                case WAITING_FOR_PHOTO -> new Message(MessageKey.EXPECTING_PHOTO_ERROR, WARNING);
                case AWAITING_LOCATION -> new Message(MessageKey.EXPECTING_LOCATION_ERROR, WARNING);
                case LOOP_OPTIONS, AWAITING_DISCARD_CONFIRMATION,
                     SUBMISSION_LOOP_OPTIONS, AWAITING_PROPOSAL_ACTION
                        -> new Message(MessageKey.INVALID_SELECTION, WARNING);
                case AWAITING_PHOTO_ANALYSIS -> new Message(MessageKey.ERROR_PROCESSING_PHOTO, WARNING);
                case AWAITING_MONUMENT_SUGGESTIONS -> new Message(MessageKey.ERROR_GENERIC, WARNING);
                default -> null;
            };
        } else if (state instanceof VerificationState verificationState) {
            return switch (verificationState) {
                case AWAITING_VERIFICATION_CODE -> new Message(MessageKey.INVALID_CODE_ERROR, WARNING);
                case AWAITING_CONTACT -> new Message(MessageKey.USER_NOT_FOUND_ERROR, WARNING);
                case AWAITING_VERIFICATION_METHOD, AWAITING_PHONE_CONNECTION_DECISION ->
                        new Message(MessageKey.INVALID_SELECTION, WARNING);
                default -> null;
            };
        }
        return null;
    }
}
