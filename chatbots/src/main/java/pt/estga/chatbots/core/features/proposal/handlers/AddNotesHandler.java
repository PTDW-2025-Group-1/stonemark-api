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
import pt.estga.proposals.services.MarkOccurrenceProposalSubmissionService;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AddNotesHandler implements ConversationStateHandler {

    private final MarkOccurrenceProposalFlowService proposalFlowService;
    private final MarkOccurrenceProposalSubmissionService submissionService;

    @Override
    public BotResponse handle(ConversationContext context, BotInput input) {
        proposalFlowService.addNotesToProposal(context.getProposal().getId(), input.getText());
        submissionService.submit(context.getProposal().getId());
        context.setProposal(null);
        context.setCurrentState(ConversationState.START);

        Menu thankYouMenu = Menu.builder()
                .title("Thank you for your submission!")
                .buttons(List.of(
                        List.of(Button.builder().text("Back to Main Menu").callbackData(CallbackData.BACK_TO_MAIN_MENU).build())
                ))
                .build();

        return BotResponse.builder()
                .uiComponent(thankYouMenu)
                .build();
    }

    @Override
    public ConversationState canHandle() {
        return ConversationState.AWAITING_NOTES;
    }
}
