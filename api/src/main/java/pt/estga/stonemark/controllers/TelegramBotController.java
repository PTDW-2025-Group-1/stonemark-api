package pt.estga.stonemark.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.stonemark.bots.telegram.StonemarkTelegramBot;

@RestController
@RequiredArgsConstructor
public class TelegramBotController {

    private final StonemarkTelegramBot telegramBot;

    @PostMapping("${telegram.bot.webhook-path}")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}
