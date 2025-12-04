package pt.estga.bots.telegram.services;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import pt.estga.shared.models.Location;

public interface TelegramBotCommandService {

    BotApiMethod<?> handleStartCommand(long chatId);

    BotApiMethod<?> handleHelpCommand(long chatId);

    BotApiMethod<?> handleCancelCommand(long chatId);

    BotApiMethod<?> handleSubmitCommand(long chatId);

    BotApiMethod<?> handlePhotoSubmission(long chatId, byte[] photoData, String fileName);

    BotApiMethod<?> handleLocationSubmission(long chatId, Location location);

    BotApiMethod<?> handleTextMessage(long chatId, String text);

    BotApiMethod<?> handleCallbackQuery(long chatId, String callbackQueryId, String callbackData);

    BotApiMethod<?> handleSkipNotesCommand(long chatId);

}
