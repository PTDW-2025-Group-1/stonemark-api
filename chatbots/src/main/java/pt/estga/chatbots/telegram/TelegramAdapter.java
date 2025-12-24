package pt.estga.chatbots.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Button;
import pt.estga.chatbots.core.shared.models.ui.ContactRequest;
import pt.estga.chatbots.core.shared.models.ui.LocationRequest;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.chatbots.core.shared.models.ui.PhotoItem;
import pt.estga.chatbots.core.shared.models.ui.TextMessage;
import pt.estga.chatbots.core.shared.models.ui.UIComponent;
import pt.estga.chatbots.core.shared.utils.TextTemplateParser;
import pt.estga.chatbots.telegram.services.TelegramFileService;
import pt.estga.chatbots.telegram.services.TelegramTextService;
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
    private final TelegramTextService textService;
    private final TextTemplateParser parser;

    public BotInput toBotInput(Update update) {
        log.debug("Converting Update to BotInput: {}", update);
        if (update.hasMessage()) {
            var message = update.getMessage();
            long chatId = message.getChatId();

            if (message.hasDocument() && message.getDocument().getMimeType() != null && message.getDocument().getMimeType().startsWith("image/")) {
                log.debug("Update contains an image document");
                return createImageInput(chatId, message.getDocument());
            } else if (message.hasPhoto()) {
                log.debug("Update contains a photo");
                return createImageInput(chatId, message.getPhoto());
            } else if (message.hasText()) {
                log.debug("Update contains text message: {}", message.getText());
                return BotInput.builder()
                        .userId(String.valueOf(message.getFrom().getId()))
                        .chatId(chatId)
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.TEXT)
                        .text(message.getText())
                        .build();
            } else if (message.hasContact()) {
                log.debug("Update contains contact: {}", message.getContact().getPhoneNumber());
                return BotInput.builder()
                        .userId(String.valueOf(message.getFrom().getId()))
                        .chatId(chatId)
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.CONTACT)
                        .text(message.getContact().getPhoneNumber())
                        .build();
            } else if (message.hasLocation()) {
                log.debug("Update contains location: {}, {}", message.getLocation().getLatitude(), message.getLocation().getLongitude());
                return BotInput.builder()
                        .userId(String.valueOf(message.getFrom().getId()))
                        .chatId(chatId)
                        .platform("TELEGRAM")
                        .type(BotInput.InputType.LOCATION)
                        .location(new Location(
                                message.getLocation().getLatitude(),
                                message.getLocation().getLongitude()
                        ))
                        .build();
            }
        } else if (update.hasCallbackQuery()) {
            log.debug("Update contains callback query: {}", update.getCallbackQuery().getData());
            return BotInput.builder()
                    .userId(String.valueOf(update.getCallbackQuery().getFrom().getId()))
                    .chatId(update.getCallbackQuery().getMessage().getChatId())
                    .platform("TELEGRAM")
                    .type(BotInput.InputType.CALLBACK)
                    .callbackData(update.getCallbackQuery().getData())
                    .build();
        }
        log.warn("Update type not supported or empty: {}", update);
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
                    .chatId(chatId)
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

    public List<PartialBotApiMethod<?>> toBotApiMethod(long chatId, BotResponse response) {
        log.debug("Converting BotResponse to BotApiMethods for chat: {}", chatId);
        List<PartialBotApiMethod<?>> methods = new ArrayList<>();
        UIComponent uiComponent = response.getUiComponent();

        if (uiComponent instanceof Menu) {
            log.debug("Rendering Menu component");
            methods.add(renderMenu(String.valueOf(chatId), (Menu) uiComponent, response.getText()));
        } else {
            if (response.getTextNode() != null) {
                log.debug("Rendering text node response");
                methods.add(new SendMessage(
                        String.valueOf(chatId),
                        textService.render(response.getTextNode())
                ));
            } else if (response.getText() != null) {
                log.debug("Rendering plain text response");
                methods.add(new SendMessage(String.valueOf(chatId), response.getText()));
            }
            if (uiComponent instanceof LocationRequest) {
                log.debug("Rendering LocationRequest component");
                methods.add(renderLocationRequest(String.valueOf(chatId), (LocationRequest) uiComponent));
            } else if (uiComponent instanceof ContactRequest) {
                log.debug("Rendering ContactRequest component");
                methods.add(renderContactRequest(String.valueOf(chatId), (ContactRequest) uiComponent));
            } else if (uiComponent instanceof PhotoItem) {
                log.debug("Rendering PhotoItem component");
                methods.addAll(renderPhotoItem(String.valueOf(chatId), (PhotoItem) uiComponent));
            } else if (uiComponent instanceof TextMessage textMessage) {
                log.debug("Rendering TextMessage component");
                methods.add(new SendMessage(String.valueOf(chatId), textService.render(textMessage.getTextNode())));
            }
        }
        return methods;
    }

    private SendMessage renderMenu(String chatId, Menu menu, String text) {
        String messageText = (text != null) ? text : textService.render(menu.getTitleNode());
        SendMessage sendMessage = new SendMessage(chatId, messageText);
        if (menu.getButtons() != null && !menu.getButtons().isEmpty()) {
            sendMessage.setReplyMarkup(createInlineKeyboardMarkup(menu.getButtons()));
        }
        return sendMessage;
    }

    private SendMessage renderLocationRequest(String chatId, LocationRequest locationRequest) {
        log.debug("Parsed messageNode: {}", locationRequest.getMessageNode());
        String messageText = textService.render(locationRequest.getMessageNode());
        SendMessage message = new SendMessage(chatId, messageText);
        message.setReplyMarkup(createReplyKeyboardMarkup("Send my location", true, false));
        return message;
    }

    private SendMessage renderContactRequest(String chatId, ContactRequest contactRequest) {
        String messageText = textService.render(contactRequest.getMessageNode());
        SendMessage message = new SendMessage(chatId, messageText);
        message.setReplyMarkup(createReplyKeyboardMarkup("Share my phone number", false, true));
        return message;
    }

    private List<PartialBotApiMethod<?>> renderPhotoItem(String chatId, PhotoItem item) {
        List<PartialBotApiMethod<?>> methods = new ArrayList<>();
        
        // Only add a button if callbackData is not "noop"
        InlineKeyboardMarkup markup = null;
        if (item.getCallbackData() != null && !"noop".equals(item.getCallbackData())) {
            markup = createSingleButtonInlineMarkup("Select", item.getCallbackData());
        }
        
        String caption = textService.render(item.getCaptionNode());

        if (item.getMediaFileId() != null) {
            InputFile inputFile = fileService.createInputFileFromMediaId(item.getMediaFileId());
            if (inputFile != null) {
                SendPhoto photo = new SendPhoto();
                photo.setChatId(chatId);
                photo.setCaption(caption);
                photo.setPhoto(inputFile);
                if (markup != null) {
                    photo.setReplyMarkup(markup);
                }
                methods.add(photo);
            } else {
                log.warn("Media file ID {} could not be resolved to an InputFile", item.getMediaFileId());
                SendMessage message = new SendMessage(chatId, caption + " (Image unavailable)");
                if (markup != null) {
                    message.setReplyMarkup(markup);
                }
                methods.add(message);
            }
        } else {
            SendMessage message = new SendMessage(chatId, caption);
            if (markup != null) {
                message.setReplyMarkup(markup);
            }
            methods.add(message);
        }
        return methods;
    }

    private InlineKeyboardMarkup createInlineKeyboardMarkup(List<List<Button>> buttons) {
        InlineKeyboardMarkup markupInline = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        for (List<Button> row : buttons) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            for (Button button : row) {
                InlineKeyboardButton inlineButton = new InlineKeyboardButton();
                inlineButton.setText(textService.render(button.getTextNode()));
                inlineButton.setCallbackData(button.getCallbackData());
                rowInline.add(inlineButton);
            }
            rowsInline.add(rowInline);
        }

        markupInline.setKeyboard(rowsInline);
        return markupInline;
    }

    private InlineKeyboardMarkup createSingleButtonInlineMarkup(String text, String callbackData) {
        return createInlineKeyboardMarkup(List.of(List.of(Button.builder().textNode(parser.parse(text)).callbackData(callbackData).build())));
    }

    private ReplyKeyboardMarkup createReplyKeyboardMarkup(String buttonText, boolean requestLocation, boolean requestContact) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText(buttonText);
        if (requestLocation) button.setRequestLocation(true);
        if (requestContact) button.setRequestContact(true);
        row.add(button);
        keyboard.add(row);
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setOneTimeKeyboard(true);
        return keyboardMarkup;
    }
}
