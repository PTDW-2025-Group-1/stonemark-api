package pt.estga.chatbots.telegram.services;

import jakarta.inject.Provider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import pt.estga.chatbots.telegram.StonemarkTelegramBot;

import java.io.File;

@Service
@RequiredArgsConstructor
@Slf4j
public class TelegramFileService {

    private final Provider<StonemarkTelegramBot> botProvider;

    public File downloadFile(String fileId) {
        try {
            StonemarkTelegramBot bot = botProvider.get();
            org.telegram.telegrambots.meta.api.objects.File file = bot.execute(new GetFile(fileId));
            return bot.downloadFile(file);
        } catch (TelegramApiException e) {
            log.error("Error downloading file with ID: {}", fileId, e);
            return null;
        }
    }
}
