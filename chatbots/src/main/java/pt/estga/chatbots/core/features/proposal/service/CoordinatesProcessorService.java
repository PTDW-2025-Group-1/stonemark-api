package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
@RequiredArgsConstructor
public class CoordinatesProcessorService {

    public BotResponse processCoordinates(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_LOCATION);
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Please provide the location.").build())
                .build();
    }
}
