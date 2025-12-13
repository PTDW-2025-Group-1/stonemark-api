package pt.estga.chatbots.telegram.services;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Contact;
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

    BotApiMethod<?> handleSkipCommand(long chatId);

    BotApiMethod<?> handleContact(long chatId, Contact contact);

}
