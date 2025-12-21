package pt.estga.chatbots.core.proposal.handlers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.shared.context.ConversationContext;
import pt.estga.chatbots.core.shared.context.ConversationState;
import pt.estga.chatbots.core.shared.context.ConversationStateHandler;
import pt.estga.chatbots.core.shared.models.BotInput;
import pt.estga.chatbots.core.shared.models.BotResponse;
import pt.estga.chatbots.core.shared.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmitLocationHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public List<BotResponse> handle(ConversationContext context, BotInput input) {
        if (input.getLocation() == null) {
            log.warn("Received non-location input when awaiting location for proposal ID: {}", context.getProposal().getId());
            return Collections.singletonList(BotResponse.builder()
                    .uiComponent(Menu.builder().title("Invalid input. Please send a location.").build())
                    .build());
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
