package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.entities.MarkOccurrenceProposal;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

@Component
@RequiredArgsConstructor
public class ConfirmMonumentHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final LoopOptionsHandler loopOptionsHandler;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        Long proposalId = context.getProposal().getId();
        String[] callbackDataParts = input.getCallbackData().split(":");
        boolean confirmed = CallbackData.CONFIRM_YES.equalsIgnoreCase(callbackDataParts[1]);

        if (confirmed) {
            Long monumentId = Long.valueOf(callbackDataParts[2]);
            MarkOccurrenceProposal updatedProposal = proposalFlowService.selectMonument(proposalId, monumentId);
            context.setProposal(updatedProposal);
            context.setCurrentState(ConversationState.LOOP_OPTIONS);
            return loopOptionsHandler.handle(context, BotInput.builder().build());
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
