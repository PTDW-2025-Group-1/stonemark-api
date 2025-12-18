package pt.estga.chatbots.core.features.proposal.commands;

import lombok.Data;
import lombok.EqualsAndHashCode;
import pt.estga.chatbots.core.infrastructure.Command;
import pt.estga.chatbots.core.models.BotInput;

@Data
@EqualsAndHashCode(callSuper = false)
public class StartSubmissionCommand implements Command {
    private final BotInput input;
}
