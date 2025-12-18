package pt.estga.chatbots.core.features.proposal.commands;

import lombok.AllArgsConstructor;
import lombok.Data;
import pt.estga.chatbots.core.infrastructure.Command;
import pt.estga.chatbots.core.models.BotInput;

@Data
@AllArgsConstructor
public class SubmitPhotoCommand implements Command {
    private BotInput input;
}
