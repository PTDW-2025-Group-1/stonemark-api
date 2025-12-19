package pt.estga.chatbots.core.features.proposal.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pt.estga.chatbots.core.context.ConversationContext;
import pt.estga.chatbots.core.context.ConversationState;
import pt.estga.chatbots.core.context.ConversationStateHandler;
import pt.estga.chatbots.core.features.common.CallbackData;
import pt.estga.chatbots.core.models.BotInput;
import pt.estga.chatbots.core.models.BotResponse;
import pt.estga.chatbots.core.models.ui.Button;
import pt.estga.chatbots.core.models.ui.Menu;
import pt.estga.proposals.services.MarkOccurrenceProposalFlowService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        proposalFlowService.addNotesToProposal(context.getProposal().getId(), input.getText());
        context.setCurrentState(ConversationState.READY_TO_SUBMIT);

        Menu submissionMenu = Menu.builder()
                .title("Your proposal is ready to submit.")
                .buttons(List.of(
                        List.of(
                                Button.builder().text("Submit").callbackData(CallbackData.SUBMIT_PROPOSAL).build()
                        )
                ))
                .build();

        return BotResponse.builder()
                .uiComponent(submissionMenu)
                .build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NOTES;
    }
}
