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
public class SelectMarkCommandHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        Long proposalId = context.getProposal().getId();
        Long markId = Long.valueOf(input.getCallbackData().split(":")[1]);
        proposalFlowService.selectMark(proposalId, markId);
        context.setCurrentState(ConversationState.READY_TO_SUBMIT);
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("Your proposal is ready to submit.").build())
                .build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_MARK_SELECTION;
    }
}
