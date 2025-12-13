package pt.estga.chatbots.telegram.model;

import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Update;

@Data
public class BotInput {
    private final Update update;
    private final long chatId;
}
