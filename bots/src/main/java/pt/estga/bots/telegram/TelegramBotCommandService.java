package pt.estga.bots.telegram;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;
import pt.estga.proposals.repositories.MarkOccurrenceProposalRepository;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramBotCommandService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService markOccurrenceProposalSubmissionService;
    private final ObjectMapper objectMapper;
    private final MarkOccurrenceProposalRepository proposalRepository;

    private final Map<Long, ProposalState> userProposalState = new HashMap<>();
    private final Map<Long, Long> activeProposals = new HashMap<>();

    public BotApiMethod<?> handleStartCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        userProposalState.put(chatId, new ProposalState(ProposalStatus.IN_PROGRESS));
        return new SendMessage(String.valueOf(chatId), BotResponses.GREETING);
    }

    public BotApiMethod<?> handleHelpCommand(Update update) {
        return new SendMessage(update.getMessage().getChatId().toString(), BotResponses.HELP_MESSAGE);
    }

    public BotApiMethod<?> handleCancelCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        userProposalState.remove(chatId);
        activeProposals.remove(chatId);
        return new SendMessage(String.valueOf(chatId), BotResponses.CANCEL_MESSAGE);
    }

    public BotApiMethod<?> handlePhotoSubmission(Update update, byte[] photoData, String fileName) {
        long chatId = update.getMessage().getChatId();
        ProposalState state = userProposalState.get(chatId);

        if (state == null || state.getStatus() != ProposalStatus.IN_PROGRESS) {
            return new SendMessage(String.valueOf(chatId), BotResponses.UNEXPECTED_PHOTO);
        }

        try {
            MarkOccurrenceProposal proposal = proposalFlowService.initiate(photoData, fileName);
            activeProposals.put(chatId, proposal.getId());
            userProposalState.put(chatId, new ProposalState(proposal.getStatus()));

            return handleProposalStatus(chatId, proposal);

        } catch (IOException e) {
            log.error("Error initiating proposal for chat {}: {}", chatId, e.getMessage());
            return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_ERROR);
        }
    }

    public BotApiMethod<?> handleLocationSubmission(Update update) {
        long chatId = update.getMessage().getChatId();
        ProposalState state = userProposalState.get(chatId);
        Long proposalId = activeProposals.get(chatId);

        if (state == null || proposalId == null || state.getStatus() != ProposalStatus.AWAITING_MONUMENT_INFO) {
            return new SendMessage(String.valueOf(chatId), BotResponses.UNEXPECTED_LOCATION);
        }

        Location location = update.getMessage().getLocation();
        MarkOccurrenceProposal proposal = proposalFlowService.proposeMonument(proposalId, "New Monument", location.getLatitude(), location.getLongitude());
        userProposalState.put(chatId, new ProposalState(proposal.getStatus()));

        return handleProposalStatus(chatId, proposal);
    }

    public BotApiMethod<?> handleTextMessage(Update update) {
        long chatId = update.getMessage().getChatId();
        String messageText = update.getMessage().getText();
        ProposalState state = userProposalState.get(chatId);
        Long proposalId = activeProposals.get(chatId);

        if (state == null || proposalId == null) {
            return new SendMessage(String.valueOf(chatId), BotResponses.UNKNOWN_COMMAND); // Or a more specific message
        }

        if (state.getStatus() == ProposalStatus.AWAITING_MARK_INFO) {
            String[] parts = messageText.split("\\n", 2);
            if (parts.length < 2) {
                return new SendMessage(String.valueOf(chatId), BotResponses.INVALID_MARK_DETAILS_FORMAT);
            }
            String title = parts[0];
            String description = parts[1];

            MarkOccurrenceProposal proposal = proposalFlowService.proposeMark(proposalId, title, description);
            userProposalState.put(chatId, new ProposalState(proposal.getStatus()));
            return handleProposalStatus(chatId, proposal);
        }

        return new SendMessage(String.valueOf(chatId), BotResponses.UNKNOWN_COMMAND);
    }

    public BotApiMethod<?> handleCallbackQuery(Update update) {
        CallbackQuery callbackQuery = update.getCallbackQuery();
        long chatId = callbackQuery.getMessage().getChatId();
        String callbackData = callbackQuery.getData();
        Long proposalId = activeProposals.get(chatId);

        if (proposalId == null) {
            return new AnswerCallbackQuery(callbackQuery.getId());
        }

        if (callbackData.startsWith("SELECT_MARK:")) {
            Long markId = Long.parseLong(callbackData.substring("SELECT_MARK:".length()));
            MarkOccurrenceProposal proposal = proposalFlowService.selectMark(proposalId, markId);
            userProposalState.put(chatId, new ProposalState(proposal.getStatus()));
            return new SendMessage(String.valueOf(chatId), BotResponses.MARK_SELECTED);
        } else if (callbackData.equals("PROPOSE_NEW_MARK")) {
            MarkOccurrenceProposal proposal = proposalRepository.getReferenceById(proposalId);
            proposal.setStatus(ProposalStatus.AWAITING_MARK_INFO); // Manually set status for flow
            userProposalState.put(chatId, new ProposalState(ProposalStatus.AWAITING_MARK_INFO));
            return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MARK_DETAILS);
        }

        return new AnswerCallbackQuery(callbackQuery.getId());
    }

    public BotApiMethod<?> handleSubmitCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        ProposalState state = userProposalState.get(chatId);
        Long proposalId = activeProposals.get(chatId);

        if (state == null || proposalId == null || state.getStatus() != ProposalStatus.READY_TO_SUBMIT) {
            return new SendMessage(String.valueOf(chatId), BotResponses.NOTHING_TO_SUBMIT);
        }

        markOccurrenceProposalSubmissionService.submit(proposalId);
        userProposalState.remove(chatId);
        activeProposals.remove(chatId);

        return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_SUCCESS);
    }

    private BotApiMethod<?> handleProposalStatus(long chatId, MarkOccurrenceProposal proposal) {
        ProposalStatus status = proposal.getStatus();
        userProposalState.put(chatId, new ProposalState(status)); // Ensure state is updated

        switch (status) {
            case AWAITING_MARK_SELECTION:
                try {
                    List<String> suggestedMarkIds = objectMapper.readValue(proposal.getSuggestedMarkIds(), new TypeReference<List<String>>() {});
                    return createMarkSelectionMessage(chatId, suggestedMarkIds);
                } catch (JsonProcessingException e) {
                    log.error("Error deserializing suggestedMarkIds for proposal {}: {}", proposal.getId(), e.getMessage());
                    return new SendMessage(String.valueOf(chatId), BotResponses.ERROR_DESERIALIZING_MARK_IDS);
                }
            case AWAITING_MONUMENT_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MONUMENT_INFO);
            case AWAITING_MARK_INFO:
                return new SendMessage(String.valueOf(chatId), BotResponses.AWAITING_MARK_DETAILS);
            case READY_TO_SUBMIT:
                return new SendMessage(String.valueOf(chatId), BotResponses.LOCATION_RECEIVED); // Generic message for now, can be more specific
            default:
                return new SendMessage(String.valueOf(chatId), "Unexpected proposal status: " + status);
        }
    }

    private SendMessage createMarkSelectionMessage(long chatId, List<String> markIds) {
        SendMessage message = new SendMessage(String.valueOf(chatId), BotResponses.SUGGESTED_MARKS_FOUND);
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (String markId : markIds) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText("Mark ID: " + markId);
            button.setCallbackData("SELECT_MARK:" + markId);
            rowInline.add(button);
            rowsInline.add(rowInline);
        }

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

    // Helper class to store proposal state
    @Setter
    @Getter
    private static class ProposalState {
        private ProposalStatus status;
        public ProposalState(ProposalStatus status) {
            this.status = status;
        }

    }
}
