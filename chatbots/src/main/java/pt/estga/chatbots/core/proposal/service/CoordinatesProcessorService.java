package pt.estga.chatbots.core.proposal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CoordinatesProcessorService {

    public List<BotResponse> processCoordinates(ConversationContext context) {
        context.setCurrentState(ConversationState.AWAITING_LOCATION);
        return Collections.singletonList(BotResponse.builder()
                .uiComponent(Menu.builder().title("Please provide the location.").build())
                .build());
    }
}
