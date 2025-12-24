package pt.estga.chatbots.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.shared.models.ServiceAccountPrincipal;
import pt.estga.shared.utils.ServiceAccountUtils;

@RestController
@RequiredArgsConstructor
public class TelegramBotWebhook {

    private final StonemarkTelegramBot telegramBot;

    private static final Long BOT_SERVICE_ACCOUNT_ID = 1000L;

    @PostMapping("${telegram.bot.webhook-path}")
    public BotApiMethod<?> handleUpdate(@RequestBody Update update) {
        ServiceAccountPrincipal botPrincipal = ServiceAccountPrincipal.builder()
                .id(BOT_SERVICE_ACCOUNT_ID)
                .serviceName("TelegramBot")
                .build();

        try {
            return ServiceAccountUtils.runAsServiceAccount(botPrincipal,
                    update.getMessage() != null ? update.getMessage().getFrom().getId() : null,
                    () -> telegramBot.onWebhookUpdateReceived(update));
        } catch (Exception e) {
            throw new RuntimeException("Error processing Telegram update", e);
        }
    }
}
