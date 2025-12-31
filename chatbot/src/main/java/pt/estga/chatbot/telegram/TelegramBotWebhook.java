package pt.estga.chatbot.telegram;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import pt.estga.shared.enums.PrincipalType;
import pt.estga.shared.models.AppPrincipal;
import pt.estga.shared.utils.ServiceAccountUtils;

import java.util.Collections;

@RestController
@RequiredArgsConstructor
public class TelegramBotWebhook {

    private final StonemarkTelegramBot telegramBot;

    private static final Long BOT_SERVICE_ACCOUNT_ID = 2L;

    @PostMapping("${telegram.bot.webhook-path}")
    public BotApiMethod<?> handleUpdate(@RequestBody Update update) {
        AppPrincipal botPrincipal = AppPrincipal.builder()
                .id(BOT_SERVICE_ACCOUNT_ID)
                .type(PrincipalType.SERVICE)
                .identifier("TelegramBot")
                .password(null)
                .authorities(Collections.emptyList())
                .enabled(true)
                .accountNonLocked(true)
                .build();

        try {
            return ServiceAccountUtils.runAsServiceAccount(botPrincipal, () -> telegramBot.onWebhookUpdateReceived(update));
        } catch (Exception e) {
            throw new RuntimeException("Error processing Telegram update", e);
        }
    }
}
