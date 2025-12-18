package pt.estga.chatbots.core.features.common.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.features.common.commands.HelpCommand;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
public class HelpCommandHandler implements CommandHandler<HelpCommand> {
    @Override
    public BotResponse handle(HelpCommand command) {
        String helpText = "You can start a new submission by sending a photo. " +
                "Use /start to restart the conversation at any time.";
        return BotResponse.builder()
                .uiComponent(Menu.builder().title(helpText).build())
                .build();
    }
}
