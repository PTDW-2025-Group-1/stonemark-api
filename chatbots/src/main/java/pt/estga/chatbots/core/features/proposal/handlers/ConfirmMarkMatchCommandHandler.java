package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchCommandHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        MarkOccurrenceProposal proposal = context.getProposal();
        boolean matches = "yes".equalsIgnoreCase(input.getCallbackData().split(":")[1]);

        if (matches) {
            // For now, we'll assume the best match is already linked in the proposal entity.
            // A more robust implementation would pass the matched mark ID in the command.
            context.setCurrentState(ConversationState.WAITING_FOR_COORDINATES_HANDLING);

            if (proposal.getLatitude() != null && proposal.getLongitude() != null) {
                // Coordinates are found
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
                // Coordinates are not found
                context.setCurrentState(ConversationState.AWAITING_LOCATION);
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("Please provide the location.").build())
                        .build();
            }
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MARK_NAME);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please enter the name for this new mark.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MARK_CONFIRMATION;
    }
}
