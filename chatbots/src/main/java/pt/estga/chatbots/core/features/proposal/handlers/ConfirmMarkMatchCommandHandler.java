package pt.estga.chatbots.core.features.proposal.handlers;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.infrastructure.CommandHandler;
import pt.estga.chatbots.core.features.proposal.commands.ConfirmMarkMatchCommand;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConfirmMarkMatchCommandHandler implements CommandHandler<ConfirmMarkMatchCommand> {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final Cache<String, ConversationContext> conversationContexts;

    @Override
    public BotResponse handle(ConfirmMarkMatchCommand command) {
        ConversationContext context = conversationContexts.get(command.getInput().getUserId(), k -> new ConversationContext());
        MarkOccurrenceProposal proposal = context.getProposal();

        if (command.isMatches()) {
            // For now, we'll assume the best match is already linked in the proposal entity.
            // A more robust implementation would pass the matched mark ID in the command.
            context.setCurrentStateName("WAITING_FOR_COORDINATES_HANDLING");

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
                context.setCurrentStateName("AWAITING_LOCATION");
                return BotResponse.builder()
                        .uiComponent(Menu.builder().title("Please provide the location.").build())
                        .build();
            }
        } else {
            context.setCurrentStateName("AWAITING_NEW_MARK_NAME");
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please enter the name for this new mark.").build())
                    .build();
        }
    }
}
