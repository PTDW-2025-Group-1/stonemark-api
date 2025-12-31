package pt.estga.chatbot.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@RestController
@RequiredArgsConstructor
public class TelegramBotWebhook {

    private final StonemarkTelegramBot telegramBot;

    @PostMapping("${telegram.bot.webhook-path}")
    public BotApiMethod<?> handleUpdate(@RequestBody Update update) {
        try {
            return telegramBot.onWebhookUpdateReceived(update);
        } catch (Exception e) {
            throw new RuntimeException("Error processing Telegram update", e);
        }
    }
}
