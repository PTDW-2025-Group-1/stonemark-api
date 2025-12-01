package pt.estga.bots.telegram;

import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class StonemarkTelegramBot extends TelegramWebhookBot {

    private final String botUsername;
    private final String botPath;
    private final TelegramBotCommandService commandService;

    private final Map<String, Function<Update, BotApiMethod<?>>> commandRegistry;

    public StonemarkTelegramBot(String botUsername, String botToken, String botPath, TelegramBotCommandService commandService) {
        super(botToken);
        this.botUsername = botUsername;
        this.botPath = botPath;
        this.commandService = commandService;

        // Register bot commands
        this.commandRegistry = Map.of(
                "/start", commandService::handleStartCommand,
                "/submit", commandService::handleSubmitCommand,
                "/cancel", commandService::handleCancelCommand,
                "/help", commandService::handleHelpCommand
        );

        setBotCommands();
    }

    private void setBotCommands() {
        List<BotCommand> commands = List.of(
                new BotCommand("start", "Start a new submission"),
                new BotCommand("submit", "Finalize a submission"),
                new BotCommand("cancel", "Cancel current operation"),
                new BotCommand("help", "Show this message")
        );

        try {
            this.execute(new SetMyCommands(commands, null, null));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasPhoto()) {
                String fileId = update.getMessage().getPhoto().stream()
                        .max(Comparator.comparing(PhotoSize::getFileSize))
                        .map(PhotoSize::getFileId)
                        .orElse(null);
                return handlePhoto(update, fileId, fileId + ".jpg");
            }

            if (update.getMessage().hasDocument()) {
                Document document = update.getMessage().getDocument();
                if (document.getMimeType() != null && document.getMimeType().startsWith("image/")) {
                    return handlePhoto(update, document.getFileId(), document.getFileName());
                }
            }

            if (update.getMessage().hasLocation()) {
                return commandService.handleLocationSubmission(update);
            }

            if (update.getMessage().hasText()) {
                String messageText = update.getMessage().getText();

                if (messageText.startsWith("/")) {
                    String command = messageText.split(" ")[0];
                    Function<Update, BotApiMethod<?>> action = commandRegistry.get(command);

                    if (action != null) {
                        return action.apply(update);
                    } else {
                        return handleUnknownCommand(update);
                    }
                }
            }
        }
        return null;
    }

    private BotApiMethod<?> handlePhoto(Update update, String fileId, String fileName) {
        if (fileId == null) {
            return handleUnknownCommand(update);
        }

        try {
            org.telegram.telegrambots.meta.api.objects.File file = execute(new GetFile(fileId));
            java.io.File downloadedFile = downloadFile(file);
            byte[] photoData = Files.readAllBytes(downloadedFile.toPath());
            return commandService.handlePhotoSubmission(update, photoData, fileName);
        } catch (TelegramApiException | IOException e) {
            e.printStackTrace();
            return new SendMessage(update.getMessage().getChatId().toString(), BotResponses.PHOTO_PROCESSING_ERROR);
        }
    }

    private BotApiMethod<?> handleUnknownCommand(Update update) {
        return new SendMessage(update.getMessage().getChatId().toString(), BotResponses.UNKNOWN_COMMAND);
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
