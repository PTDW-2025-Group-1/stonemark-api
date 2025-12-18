package pt.estga.chatbots.core.features.proposal.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CoordinatesProcessorService {

    public BotResponse processCoordinates(ConversationContext context) {
        MarkOccurrenceProposal proposal = context.getProposal();
        context.setCurrentState(ConversationState.WAITING_FOR_COORDINATES_HANDLING);

        if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
            Menu coordinatesMenu = Menu.builder()
                    .title("We detected coordinates in the photo. How would you like to set the location?")
                    .buttons(List.of(
                            List.of(
                                    Button.builder().text("📍 Use detected coordinates").callbackData("use_detected_coordinates").build(),
                                    Button.builder().text("🖐 Send location manually").callbackData("send_location_manually").build()
                            )
                    ))
                    .build();
            return BotResponse.builder().uiComponent(coordinatesMenu).build();
        } else {
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please provide the location.").build())
                    .build();
        }
    }
}
