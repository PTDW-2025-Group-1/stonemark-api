package pt.estga.chatbots.telegram.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.chatbots.telegram.BotResponses;
import pt.estga.chatbots.telegram.StonemarkTelegramBot;
import pt.estga.chatbots.telegram.services.TelegramBotCommandService;
import pt.estga.shared.models.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class MessageHandler implements UpdateHandler {

    private final StonemarkTelegramBot bot;
    private final TelegramBotCommandService commandService;
    private final CommandHandler commandHandler;

    public MessageHandler(StonemarkTelegramBot bot, TelegramBotCommandService commandService) {
        this.bot = bot;
        this.commandService = commandService;
        this.commandHandler = new CommandHandler(commandService);
    }

    @Override
    public BotApiMethod<?> handle(Update update) {
        Message message = update.getMessage();
        long chatId = message.getChatId();

        if (message.hasText() && message.getText().startsWith("/")) {
            return commandHandler.handle(message);
        }

        if (message.hasContact()) {
            return commandService.handleContact(chatId, message.getContact());
        }

        if (message.hasPhoto()) {
            return handlePhotoMessage(chatId, message.getPhoto());
        }

        if (message.hasDocument()) {
            Document document = message.getDocument();
            if (document.getMimeType() != null && document.getMimeType().startsWith("image/")) {
                return processPhoto(chatId, document.getFileId(), document.getFileName());
            }
        }

        if (message.hasLocation()) {
            org.telegram.telegrambots.meta.api.objects.Location telegramLocation = message.getLocation();
            Location location = new Location(telegramLocation.getLatitude(), telegramLocation.getLongitude());
            return commandService.handleLocationSubmission(chatId, location);
        }

        if (message.hasText()) {
            return commandService.handleTextMessage(chatId, message.getText());
        }

        return new SendMessage(String.valueOf(chatId), BotResponses.UNKNOWN_COMMAND_HELP);
    }

    private BotApiMethod<?> handlePhotoMessage(long chatId, List<PhotoSize> photos) {
        Optional<PhotoSize> largestPhoto = photos.stream()
                .max(Comparator.comparing(photo -> photo.getWidth() * photo.getHeight()));

        if (largestPhoto.isPresent()) {
            String fileId = largestPhoto.get().getFileId();
            String fileName = fileId + ".jpg";
            return processPhoto(chatId, fileId, fileName);
        } else {
            log.warn("Received a photo message with no photos for chat: {}", chatId);
            return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_PROCESSING_ERROR);
        }
    }

    private BotApiMethod<?> processPhoto(long chatId, String fileId, String fileName) {
        try {
            bot.execute(new SendMessage(String.valueOf(chatId), BotResponses.SEARCHING_FOR_MATCHES));

            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(new GetFile(fileId));
            java.io.File downloadedFile = bot.downloadFile(file);
            byte[] photoData = Files.readAllBytes(downloadedFile.toPath());
            return commandService.handlePhotoSubmission(chatId, photoData, fileName);
        } catch (TelegramApiException | IOException e) {
            log.error("Error processing photo with fileId: {}", fileId, e);
            return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_PROCESSING_ERROR);
        }
    }
}
