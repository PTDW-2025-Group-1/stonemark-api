package pt.estga.chatbots.core.features.common.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.estga.chatbots.core.infrastructure.Command;
import pt.estga.chatbots.core.models.BotInput;

@Data
@AllArgsConstructor
public class StartCommand implements Command {
    private BotInput input;
}
