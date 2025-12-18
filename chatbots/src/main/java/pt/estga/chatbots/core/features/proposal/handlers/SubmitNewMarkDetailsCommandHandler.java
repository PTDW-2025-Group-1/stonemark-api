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
public class SubmitNewMarkDetailsCommandHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        Long proposalId = context.getProposal().getId();
        String details = input.getText();

        proposalFlowService.proposeMark(proposalId, details, details);
        context.setCurrentState(ConversationState.READY_TO_SUBMIT);
        return BotResponse.builder()
                .uiComponent(Menu.builder().title("New mark details received. Your submission is ready.").build())
                .build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NEW_MARK_DETAILS;
    }
}
