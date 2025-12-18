package pt.estga.chatbots.core.infrastructure;

import pt.estga.chatbots.core.models.BotResponse;

public interface CommandHandler<T extends Command> {
    BotResponse handle(T command);
}
