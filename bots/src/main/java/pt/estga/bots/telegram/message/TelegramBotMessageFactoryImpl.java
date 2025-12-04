package pt.estga.bots.telegram.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import pt.estga.bots.telegram.BotResponses;
import pt.estga.content.entities.Mark;
import pt.estga.content.services.MarkService;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramBotMessageFactoryImpl implements TelegramBotMessageFactory {

    private final ObjectMapper objectMapper;
    private final MarkService markService;

    @Override
    public BotApiMethod<?> createGreetingMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.GREETING);
    }

    @Override
    public BotApiMethod<?> createHelpMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.HELP_MESSAGE);
    }

    @Override
    public BotApiMethod<?> createCancelMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.CANCEL_MESSAGE);
    }

    @Override
    public BotApiMethod<?> createInvalidInputForStateMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.INVALID_INPUT_FOR_STATE);
    }

    @Override
    public BotApiMethod<?> createPhotoErrorMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_ERROR);
    }

    @Override
    public BotApiMethod<?> createUnknownCommandHelpMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.UNKNOWN_COMMAND_HELP);
    }

    @Override
    public BotApiMethod<?> createInvalidMarkDetailsFormatMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.INVALID_MARK_DETAILS_FORMAT);
    }

    @Override
    public BotApiMethod<?> createNothingToSubmitMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.NOTHING_TO_SUBMIT);
    }

    @Override
    public BotApiMethod<?> createSubmissionNotReadyMessage(long chatId, ProposalStatus status) {
        switch (status) {
            case AWAITING_MONUMENT_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_AWAITING_MONUMENT_INFO);
            case AWAITING_MARK_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_AWAITING_MARK_INFO);
            default:
                return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_NOT_READY);
        }
    }

    @Override
    public BotApiMethod<?> createSubmissionSuccessMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_SUCCESS);
    }

    @Override
    public BotApiMethod<?> createMessageForProposalStatus(long chatId, MarkOccurrenceProposal proposal) {
        ProposalStatus status = proposal.getStatus();

        switch (status) {
            case AWAITING_MARK_SELECTION:
                try {
                    List<String> suggestedMarkIds = objectMapper.readValue(proposal.getSuggestedMarkIds(), new TypeReference<List<String>>() {});
                    return createMarkSelectionMessage(chatId, suggestedMarkIds);
                } catch (JsonProcessingException e) {
                    log.error("Error deserializing suggestedMarkIds for proposal {}: {}", proposal.getId(), e.getMessage());
                    return new SendMessage(String.valueOf(chatId), BotResponses.ERROR_DESERIALIZING_MARK_IDS);
                }
            case AWAITING_MONUMENT_SELECTION:
                try {
                    List<String> suggestedMonumentIds = objectMapper.readValue(proposal.getSuggestedMonumentIds(), new TypeReference<List<String>>() {});
                    return createMonumentSelectionMessage(chatId, suggestedMonumentIds);
                } catch (JsonProcessingException e) {
                    log.error("Error deserializing suggestedMonumentIds for proposal {}: {}", proposal.getId(), e.getMessage());
                    return new SendMessage(String.valueOf(chatId), BotResponses.ERROR_DESERIALIZING_MONUMENT_IDS);
                }
            case AWAITING_MONUMENT_VERIFICATION:
                return createVerificationMessage(chatId);
            case AWAITING_MONUMENT_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MONUMENT_INFO);
            case AWAITING_MONUMENT_NAME:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MONUMENT_NAME);
            case AWAITING_MARK_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MARK_DETAILS);
            case AWAITING_NOTES:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_NOTES_MESSAGE);
            case READY_TO_SUBMIT:
                return new SendMessage(String.valueOf(chatId), BotResponses.READY_TO_SUBMIT_MESSAGE);
            default:
                return new SendMessage(String.valueOf(chatId), "Unexpected proposal status: " + status);
        }
    }

    @Override
    public BotApiMethod<?> createAwaitingMarkDetailsMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MARK_DETAILS);
    }

    @Override
    public BotApiMethod<?> createNothingToSkipMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.NOTHING_TO_SKIP_MESSAGE);
    }

    @Override
    public BotApiMethod<?> createAuthenticationRequestMessage(long chatId) {
        SendMessage message = new SendMessage(String.valueOf(chatId), BotResponses.AUTHENTICATION_REQUEST);
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("Share my phone number");
        button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    @Override
    public BotApiMethod<?> createAuthenticationSuccessMessage(long chatId, String name) {
        return new SendMessage(String.valueOf(chatId), String.format(BotResponses.AUTHENTICATION_SUCCESS, name));
    }

    @Override
    public BotApiMethod<?> createAuthenticationFailedMessage(long chatId) {
        return new SendMessage(String.valueOf(chatId), BotResponses.AUTHENTICATION_FAILED);
    }

    @Override
    public BotApiMethod<?> createWelcomeBackMessage(long chatId, String name) {
        return new SendMessage(String.valueOf(chatId), String.format(BotResponses.WELCOME_BACK, name));
    }

    private SendMessage createMarkSelectionMessage(long chatId, List<String> markIds) {
        SendMessage message = new SendMessage(String.valueOf(chatId), BotResponses.SUGGESTED_MARKS_FOUND);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (String markId : markIds) {
            Optional<Mark> markOptional = markService.findById(Long.valueOf(markId));
            if (markOptional.isPresent()) {
                Mark mark = markOptional.get();
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(mark.getTitle());
                button.setCallbackData("SELECT_MARK:" + markId);
                rowInline.add(button);
                rowsInline.add(rowInline);
            } else {
                log.warn("Mark with ID {} not found when creating selection message.", markId);
            }
        }

        // TODO: Photo display.

        List<InlineKeyboardButton> proposeNewRow = new ArrayList<>();
        InlineKeyboardButton proposeNewButton = new InlineKeyboardButton();
        proposeNewButton.setText("Propose New Mark");
        proposeNewButton.setCallbackData("PROPOSE_NEW_MARK");
        proposeNewRow.add(proposeNewButton);
        rowsInline.add(proposeNewRow);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    private SendMessage createMonumentSelectionMessage(long chatId, List<String> monumentIds) {
        SendMessage message = new SendMessage(String.valueOf(chatId), BotResponses.SUGGESTED_MONUMENTS_FOUND);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (String monumentId : monumentIds) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Monument ID: " + monumentId);
            button.setCallbackData("SELECT_MONUMENT:" + monumentId);
            rowInline.add(button);
            rowsInline.add(rowInline);
        }

        List<InlineKeyboardButton> proposeNewRow = new ArrayList<>();
        InlineKeyboardButton proposeNewButton = new InlineKeyboardButton();
        proposeNewButton.setText("Propose New Monument");
        proposeNewButton.setCallbackData("PROPOSE_NEW_MONUMENT");
        proposeNewRow.add(proposeNewButton);
        rowsInline.add(proposeNewRow);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }

    private SendMessage createVerificationMessage(long chatId) {
        SendMessage message = new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MONUMENT_VERIFICATION);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> rowInline = new ArrayList<>();

        InlineKeyboardButton yesButton = new InlineKeyboardButton();
        yesButton.setText("Yes");
        yesButton.setCallbackData("CONFIRM_MONUMENT_LOCATION:YES");

        InlineKeyboardButton noButton = new InlineKeyboardButton();
        noButton.setText("No");
        noButton.setCallbackData("CONFIRM_MONUMENT_LOCATION:NO");

        rowInline.add(yesButton);
        rowInline.add(noButton);
        rowsInline.add(rowInline);

        markupInline.setKeyboard(rowsInline);
        message.setReplyMarkup(markupInline);
        return message;
    }
}
