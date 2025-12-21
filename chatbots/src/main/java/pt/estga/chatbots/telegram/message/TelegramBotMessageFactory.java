package pt.estga.chatbots.telegram.message;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.content.entities.Monument;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.enums.ProposalStatus;

import java.util.List;

public interface TelegramBotMessageFactory {

    BotApiMethod<?> createGreetingMessage(long chatId);

    BotApiMethod<?> createHelpMessage(long chatId);

    BotApiMethod<?> createCancelMessage(long chatId);

    BotApiMethod<?> createInvalidInputForStateMessage(long chatId);

    BotApiMethod<?> createPhotoErrorMessage(long chatId);

    BotApiMethod<?> createUnknownCommandHelpMessage(long chatId);

    BotApiMethod<?> createInvalidMarkDetailsFormatMessage(long chatId);

    BotApiMethod<?> createNothingToSubmitMessage(long chatId);

    BotApiMethod<?> createSubmissionNotReadyMessage(long chatId, ProposalStatus status);

    BotApiMethod<?> createSubmissionSuccessMessage(long chatId);

    BotApiMethod<?> createMessageForProposalStatus(long chatId, MarkOccurrenceProposal proposal);

    BotApiMethod<?> createAwaitingMarkDetailsMessage(long chatId);

    BotApiMethod<?> createNothingToSkipMessage(long chatId);

    BotApiMethod<?> createAuthenticationRequestMessage(long chatId);

    BotApiMethod<?> createAuthenticationSuccessMessage(long chatId, String name);

    BotApiMethod<?> createAuthenticationFailedMessage(long chatId);

    BotApiMethod<?> createWelcomeBackMessage(long chatId, String name);

    BotApiMethod<?> createMonumentSelectionMessage(long chatId, List<Monument> monuments);

    BotApiMethod<?> createInvalidInputMessage(long chatId);
}
