package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.proposal.service.LocationProcessorService;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class UseDetectedCoordinatesHandler implements ConversationStateHandler {

    private final LocationProcessorService locationProcessorService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        var proposal = context.getProposal();
        log.info("Using detected coordinates for proposal ID: {}", proposal.getId());
        return locationProcessorService.processLocation(
                context,
                proposal.getLatitude(),
                proposal.getLongitude()
        );
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_COORDINATES_HANDLING;
    }
}
