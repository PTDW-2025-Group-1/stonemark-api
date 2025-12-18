package pt.estga.chatbots.core.features.auth.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.estga.chatbots.core.infrastructure.Command;
import pt.estga.chatbots.core.models.BotInput;

@Data
@AllArgsConstructor
public class SubmitContactCommand implements Command {
    private BotInput input;
}
