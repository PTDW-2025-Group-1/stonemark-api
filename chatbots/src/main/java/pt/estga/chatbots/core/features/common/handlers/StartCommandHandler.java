package pt.estga.chatbots.core.features.common.handlers;

import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.common.commands.StartCommand;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;

import java.util.List;

@Component
public class StartCommandHandler implements CommandHandler<StartCommand> {
    @Override
    public BotResponse handle(StartCommand command) {
        Menu mainMenu = Menu.builder()
                .title("Welcome! What would you like to do?")
                .buttons(List.of(
                        List.of(
                                Button.builder().text("New Submission").callbackData("start_submission").build(),
                                Button.builder().text("Verify Account").callbackData("start_verification").build()
                        )
                ))
                .build();

        return BotResponse.builder()
                .uiComponent(mainMenu)
                .build();
    }
}
