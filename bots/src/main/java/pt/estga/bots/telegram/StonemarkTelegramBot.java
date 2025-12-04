package pt.estga.bots.telegram;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.bots.telegram.services.TelegramBotCommandService;
import pt.estga.shared.models.Location;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;
    private final TelegramBotCommandService commandService;
    private final Map<String, Function<Long, BotApiMethod<?>>> commandRegistry;

    public StonemarkTelegramBot(String botUsername, String botToken, String botPath, TelegramBotCommandService commandService) {
        super(botToken);
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.commandService = commandService;

        this.commandRegistry = Map.of(
                "/start", commandService::handleStartCommand,
                "/submit", commandService::handleSubmitCommand,
                "/cancel", commandService::handleCancelCommand,
                "/help", commandService::handleHelpCommand,
                "/skipnotes", commandService::handleSkipNotesCommand
        );

        setBotCommands();
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "Start a new submission"),
                new BotCommand("submit", "Finalize a submission"),
                new BotCommand("cancel", "Cancel current operation"),
                new BotCommand("help", "Show this message"),
                new BotCommand("skipnotes", "Skip adding notes to the proposal")
        );

        try {
            this.execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            log.error("Error setting bot commands", e);
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            return handleMessage(update.getMessage());
        } else if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            return commandService.handleCallbackQuery(callbackQuery.getMessage().getChatId(), callbackQuery.getId(), callbackQuery.getData());
        }
        log.warn("Received an unhandled update type: {}", update);
        return null;
    }

    private BotApiMethod<?> handleMessage(Message message) {
        long chatId = message.getChatId();

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
            return handleTextMessage(chatId, message.getText());
        }

        return new SendMessage(String.valueOf(chatId), BotResponses.UNKNOWN_COMMAND_HELP);
    }

    private BotApiMethod<?> handleTextMessage(long chatId, String text) {
        if (text != null && text.startsWith("/")) {
            String command = text.split(" ")[0];
            Function<Long, BotApiMethod<?>> action = commandRegistry.get(command);
            if (action != null) {
                return action.apply(chatId);
            }
        }
        return commandService.handleTextMessage(chatId, text);
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
            org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));
            java.io.File downloadedFile = downloadFile(file);
            byte[] photoData = Files.readAllBytes(downloadedFile.toPath());
            return commandService.handlePhotoSubmission(chatId, photoData, fileName);
        } catch (TelegramApiException | IOException e) {
            log.error("Error processing photo with fileId: {}", fileId, e);
            return new SendMessage(String.valueOf(chatId), BotResponses.PHOTO_PROCESSING_ERROR);
        }
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public String getBotPath() {
        return botPath;
    }
}
