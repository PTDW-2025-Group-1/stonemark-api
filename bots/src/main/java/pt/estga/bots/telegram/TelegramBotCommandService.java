package pt.estga.bots.telegram;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Location;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;
import pt.estga.proposals.services.ProposalSubmissionService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class TelegramBotCommandService {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final ProposalSubmissionService proposalSubmissionService;

    private final Map<Long, String> userState = new HashMap<>();
    private final Map<Long, Long> activeProposals = new HashMap<>();

    private static final String AWAITING_PHOTO = "AWAITING_PHOTO";
    private static final String AWAITING_LOCATION = "AWAITING_LOCATION";
    private static final String READY_TO_SUBMIT = "READY_TO_SUBMIT";

    public TelegramBotCommandService(MarkOccurrenceProposalFlowService proposalFlowService, ProposalSubmissionService proposalSubmissionService) {
        this.proposalFlowService = proposalFlowService;
        this.proposalSubmissionService = proposalSubmissionService;
    }

    public BotApiMethod<?> handleStartCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        userState.put(chatId, AWAITING_PHOTO);
        return new SendMessage(String.valueOf(chatId), BotResponses.GREETING);
    }

    public BotApiMethod<?> handleHelpCommand(Update update) {
        return new SendMessage(update.getMessage().getChatId().toString(), BotResponses.HELP_MESSAGE);
    }

    public BotApiMethod<?> handleCancelCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        userState.remove(chatId);
        activeProposals.remove(chatId);
        return new SendMessage(String.valueOf(chatId), BotResponses.CANCEL_MESSAGE);
    }

    public BotApiMethod<?> handlePhotoSubmission(Update update, byte[] photoData, String fileName) {
        long chatId = update.getMessage().getChatId();
        if (AWAITING_PHOTO.equals(userState.get(chatId))) {
            try {
                MarkOccurrenceProposal proposal = proposalFlowService.initiate(photoData, fileName);
                activeProposals.put(chatId, proposal.getId());
                userState.put(chatId, AWAITING_LOCATION);
                return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_RECEIVED);
            } catch (IOException e) {
                e.printStackTrace();
                return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_ERROR);
            }
        }
        return new SendMessage(String.valueOf(chatId), BotResponses.UNEXPECTED_PHOTO);
    }

    public BotApiMethod<?> handleLocationSubmission(Update update) {
        long chatId = update.getMessage().getChatId();
        if (AWAITING_LOCATION.equals(userState.get(chatId))) {
            Location location = update.getMessage().getLocation();
            Long proposalId = activeProposals.get(chatId);

            proposalFlowService.proposeMonument(proposalId, "New Monument", location.getLatitude(), location.getLongitude());
            proposalFlowService.proposeMark(proposalId, "New Mark", "Submitted via Telegram");

            userState.put(chatId, READY_TO_SUBMIT);
            return new SendMessage(String.valueOf(chatId), BotResponses.LOCATION_RECEIVED);
        }
        return new SendMessage(String.valueOf(chatId), BotResponses.UNEXPECTED_LOCATION);
    }

    public BotApiMethod<?> handleSubmitCommand(Update update) {
        long chatId = update.getMessage().getChatId();
        if (READY_TO_SUBMIT.equals(userState.get(chatId))) {
            Long proposalId = activeProposals.get(chatId);
            proposalSubmissionService.submit(proposalId);

            userState.remove(chatId);
            activeProposals.remove(chatId);

            return new SendMessage(String.valueOf(chatId), BotResponses.SUBMISSION_SUCCESS);
        }
        return new SendMessage(String.valueOf(chatId), BotResponses.NOTHING_TO_SUBMIT);
    }
}
