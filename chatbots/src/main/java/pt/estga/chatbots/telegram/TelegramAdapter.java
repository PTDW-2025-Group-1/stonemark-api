package pt.estga.chatbots.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.ContactRequest;
import pt.estga.chatbots.core.models.ui.LocationRequest;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.chatbots.core.models.ui.PhotoGallery;
import pt.estga.chatbots.core.models.ui.UIComponent;
import pt.estga.chatbots.telegram.services.TelegramFileService;
import pt.estga.shared.models.Location;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class TelegramAdapter {

    private final TelegramFileService fileService;

    public BotInput toBotInput(Update update) {
        if (update.hasMessage()) {
            var message = update.getMessage();
            long chatId = message.getChatId();

            if (message.hasDocument() && message.getDocument().getMimeType() != null && message.getDocument().getMimeType().startsWith("image/")) {
                return createImageInput(chatId, message.getDocument());
            } else if (message.hasPhoto()) {
                return createImageInput(chatId, message.getPhoto());
            } else if (message.hasText()) {
                return BotInput.builder()
                        .userId(String.valueOf(chatId))
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.TEXT)
                        .text(message.getText())
                        .build();
            } else if (message.hasContact()) {
                return BotInput.builder()
                        .userId(String.valueOf(chatId))
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.CONTACT)
                        .text(message.getContact().getPhoneNumber())
                        .build();
            } else if (message.hasLocation()) {
                return BotInput.builder()
                        .userId(String.valueOf(chatId))
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.LOCATION)
                        .location(new Location(
                                message.getLocation().getLatitude(),
                                message.getLocation().getLongitude()
                        ))
                        .build();
            }
        } else if (update.hasCallbackQuery()) {
            return BotInput.builder()
                    .userId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()))
                    .platform("TELEGRAM")
                    .type(BotInput.InputType.CALLBACK)
                    .callbackData(update.getCallbackQuery().getData())
                    .build();
        }
        return null;
    }

    private BotInput createImageInput(long chatId, List<PhotoSize> photos) {
        Optional<PhotoSize> largestPhoto = photos.stream()
                .max(Comparator.comparing(PhotoSize::getFileSize));
        if (largestPhoto.isPresent()) {
            // Note: This filename is a guess, as Telegram photos don't have one.
            return processImageFile(chatId, largestPhoto.get().getFileId(), largestPhoto.get().getFileId() + ".jpg");
        }
        log.warn("Received a photo message with no photos for chat: {}", chatId);
        return null;
    }

    private BotInput createImageInput(long chatId, Document document) {
        return processImageFile(chatId, document.getFileId(), document.getFileName());
    }

    private BotInput processImageFile(long chatId, String fileId, String fileName) {
        try {
            log.info("Processing image file with ID: {} and name: {}", fileId, fileName);
            File downloadedFile = fileService.downloadFile(fileId);
            if (downloadedFile == null || !downloadedFile.exists()) {
                log.error("Failed to download file or file does not exist. File ID: {}", fileId);
                return null;
            }
            byte[] photoData = Files.readAllBytes(downloadedFile.toPath());
            log.info("Successfully read {} bytes from file {}", photoData.length, fileName);

            return BotInput.builder()
                    .userId(String.valueOf(chatId))
                    .platform("TELEGRAM")
                    .type(BotInput.InputType.PHOTO)
                    .fileData(photoData)
                    .fileName(fileName)
                    .build();
        } catch (IOException e) {
            log.error("Error processing image file with ID: {}", fileId, e);
            return null;
        }
    }

    public List<PartialBotApiMethod<?>> toBotApiMethod(String chatId, BotResponse response) {
        UIComponent uiComponent = response.getUiComponent();
        if (uiComponent instanceof Menu) {
            return List.of(renderMenu(chatId, (Menu) uiComponent));
        } else if (uiComponent instanceof LocationRequest) {
            return List.of(renderLocationRequest(chatId, (LocationRequest) uiComponent));
        } else if (uiComponent instanceof ContactRequest) {
            return List.of(renderContactRequest(chatId, (ContactRequest) uiComponent));
        } else if (uiComponent instanceof PhotoGallery) {
            return renderPhotoGallery(chatId, (PhotoGallery) uiComponent);
        }
        return List.of();
    }

    private SendMessage renderMenu(String chatId, Menu menu) {
        SendMessage sendMessage = new SendMessage(chatId, menu.getTitle());
        if (menu.getButtons() != null && !menu.getButtons().isEmpty()) {
            InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
            List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

            for (List<Button> row : menu.getButtons()) {
                List<InlineKeyboardButton> rowInline = new ArrayList<>();
                for (Button button : row) {
                    InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                    inlineButton.setText(button.getText());
                    inlineButton.setCallbackData(button.getCallbackData());
                    rowInline.add(inlineButton);
                }
                rowsInline.add(rowInline);
            }

            markupInline.setKeyboard(rowsInline);
            sendMessage.setReplyMarkup(markupInline);
        }
        return sendMessage;
    }

    private SendMessage renderLocationRequest(String chatId, LocationRequest locationRequest) {
        SendMessage message = new SendMessage(chatId, locationRequest.getMessage());
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("Send my location");
        button.setRequestLocation(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);
        message.setReplyMarkup(keyboardMarkup);
        return message;
    }

    private SendMessage renderContactRequest(String chatId, ContactRequest contactRequest) {
        SendMessage message = new SendMessage(chatId, contactRequest.getMessage());
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

    private List<PartialBotApiMethod<?>> renderPhotoGallery(String chatId, PhotoGallery gallery) {
        // Todo: Implement photo gallery rendering
        return null;
    }
}
