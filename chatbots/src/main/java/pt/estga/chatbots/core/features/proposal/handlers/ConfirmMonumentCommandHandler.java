package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
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
public class ConfirmMonumentCommandHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        Long proposalId = context.getProposal().getId();
        String[] callbackDataParts = input.getCallbackData().split(":");
        boolean confirmed = "yes".equalsIgnoreCase(callbackDataParts[1]);

        if (confirmed) {
            Long monumentId = Long.valueOf(callbackDataParts[2]);
            proposalFlowService.selectMonument(proposalId, monumentId);
            context.setCurrentState(ConversationState.READY_TO_SUBMIT);
            // In a real scenario, you might ask for more details or confirm submission.
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Monument confirmed. Your submission is ready.").build())
                    .build();
        } else {
            context.setCurrentState(ConversationState.AWAITING_NEW_MONUMENT_NAME);
            return BotResponse.builder()
                    .uiComponent(Menu.builder().title("Understood. Please provide the name of the new monument.").build())
                    .build();
        }
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.WAITING_FOR_MONUMENT_CONFIRMATION;
    }
}
