package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.features.proposal.service.LocationProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;

@Component
@RequiredArgsConstructor
@Slf4j
public class SendLocationManuallyHandler implements ConversationStateHandler {

    private final LocationProcessorService locationProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        
        if (input.getLocation() != null) {
            log.info("Handling manually sent location for proposal ID: {}", context.getProposal().getId());
            return locationProcessorService.processLocation(
                context,
                input.getLocation().getLatitude(),
                input.getLocation().getLongitude()
            );
        }
        
        if (input.getCallbackData() != null && input.getCallbackData().equals(CallbackData.SEND_LOCATION_MANUALLY)) {
            log.info("Requesting manual location for proposal ID: {}", context.getProposal().getId());
            context.setCurrentState(ConversationState.AWAITING_LOCATION);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Please send the location.").build())
                    .build();
        }
        
        return null;
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_COORDINATES_HANDLING;
    }
}
