package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitLocationHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        if (input.getLocation() == null) {
            log.warn("Received non-location input when awaiting location for proposal ID: {}", context.getProposal().getId());
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Invalid input. Please send a location.").build())
                    .build();
        }

        log.info("Handling location submission for proposal ID: {}", context.getProposal().getId());
        proposalFlowService.addLocationToProposal(
                context.getProposal().getId(),
                input.getLocation().getLatitude(),
                input.getLocation().getLongitude()
        );

        context.setCurrentState(ConversationState.LOOP_OPTIONS);
        return loopOptionsHandler.handle(context, BotInput.builder().build());
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_LOCATION;
    }
}
